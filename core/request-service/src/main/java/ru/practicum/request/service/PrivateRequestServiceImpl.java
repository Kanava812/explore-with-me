package ru.practicum.request.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.UserActionClient;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.internal.error.exception.NotFoundException;
import ru.practicum.internal.error.exception.RuleViolationException;
import ru.practicum.internal.event.dto.EventFullDto;
import ru.practicum.internal.event.client.EventInternalClient;
import ru.practicum.internal.request.dto.RequestDto;
import ru.practicum.internal.user.dto.UserDto;
import ru.practicum.internal.event.enums.EventState;

import ru.practicum.internal.user.client.UserInternalClient;
import ru.practicum.request.dao.RequestRepository;
import ru.practicum.request.dto.RequestStatusUpdate;
import ru.practicum.request.dto.RequestStatusUpdateResult;
import ru.practicum.internal.request.enums.RequestStatus;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PrivateRequestServiceImpl implements PrivateRequestService {
    private final RequestRepository requestRepository;
    private final UserInternalClient userInternalClient;
    private final EventInternalClient eventInternalClient;
    private final RequestMapper requestMapper;
    private final EntityManager em;
    private final UserActionClient userActionClient;

    @Override
    public RequestDto createRequest(Long userId, Long eventId) {
        log.info("Creating participation request for user {} to event {}", userId, eventId);

        UserDto userDto = getUserDtoOrThrow(userId);

        EventFullDto eventDto = eventInternalClient.getByIdAndState(eventId, null);

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new RuleViolationException("Participation request already exists for user " + userId + " to event " + eventId);
        }

        if (eventDto.getInitiator().getId().equals(userId)) {
            throw new RuleViolationException("Initiator cannot request participation in their own event");
        }

        if (!eventDto.getState().equals(EventState.PUBLISHED.name())) {
            throw new RuleViolationException("Cannot participate in unpublished event");
        }

        if (eventDto.getParticipantLimit() > 0) {
            long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (confirmedRequests >= eventDto.getParticipantLimit()) {
                throw new RuleViolationException("Participant limit reached for event");
            }
        }

        Request request = new Request();
        request.setEventId(eventDto.getId());
        request.setRequesterId(userDto.getId());

        if (eventDto.getParticipantLimit() == 0 || (eventDto.getRequestModeration() != null && !eventDto.getRequestModeration())) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        Request savedRequest = requestRepository.save(request);

        em.flush();
        em.refresh(savedRequest);
        userActionClient.sendUserAction(userId, eventId, ActionTypeProto.ACTION_REGISTER, Instant.now());
        return requestMapper.toRequestDto(savedRequest);
    }

    @Override
    public RequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Cancelling request {} for user {}", requestId, userId);

        getUserDtoOrThrow(userId);

        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        if (request.getStatus() != RequestStatus.PENDING && request.getStatus() != RequestStatus.CONFIRMED) {
            throw new RuleViolationException("Only pending or confirmed requests can be cancelled");
        }

        request.setStatus(RequestStatus.CANCELED);

        Request cancelledRequest = requestRepository.save(request);

        return requestMapper.toRequestDto(cancelledRequest);
    }


    @Override
    public RequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, RequestStatusUpdate updateRequest) {
        log.info("Updating request status for eventDto {} by user {}", eventId, userId);

        getUserDtoOrThrow(userId);

        EventFullDto eventDto = eventInternalClient.getByIdAndState(eventId, null);

        RequestStatus newStatus;
        try {
            newStatus = RequestStatus.valueOf(updateRequest.getStatus());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + updateRequest.getStatus());
        }

        if (newStatus != RequestStatus.CONFIRMED && newStatus != RequestStatus.REJECTED) {
            throw new IllegalArgumentException("New status must be CONFIRMED or REJECTED");
        }

        if (eventDto.getParticipantLimit() == 0 || !eventDto.getRequestModeration()) {
            throw new RuleViolationException("Event does not require request moderation");
        }

        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        List<Request> requestsToUpdate = requestRepository.findAllById(updateRequest.getRequestIds());

        for (Request request : requestsToUpdate) {
            if (!request.getEventId().equals(eventId)) {
                throw new IllegalArgumentException("Request " + request.getId() + " does not belong to eventDto " + eventId);
            }
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new RuleViolationException("Request " + request.getId() + " must have status PENDING");
            }
        }

        if (newStatus == RequestStatus.CONFIRMED) {
            if (confirmedCount + requestsToUpdate.size() > eventDto.getParticipantLimit()) {
                throw new RuleViolationException("The participant limit has been reached");
            }
        }

        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();

        for (Request request : requestsToUpdate) {
            request.setStatus(newStatus);
            if (newStatus == RequestStatus.CONFIRMED) {
                confirmedRequests.add(request);
            } else {
                rejectedRequests.add(request);
            }
        }

        requestRepository.saveAll(requestsToUpdate);

        if (newStatus == RequestStatus.CONFIRMED && confirmedCount + confirmedRequests.size() >= eventDto.getParticipantLimit()) {
            List<Request> pendingRequests = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);

            List<Request> autoRejected = new ArrayList<>();
            for (Request pendingRequest : pendingRequests) {
                if (!updateRequest.getRequestIds().contains(pendingRequest.getId())) {
                    pendingRequest.setStatus(RequestStatus.REJECTED);
                    autoRejected.add(pendingRequest);
                    rejectedRequests.add(pendingRequest);
                }
            }

            if (!autoRejected.isEmpty()) {
                requestRepository.saveAll(autoRejected);
            }
        }

        List<RequestDto> confirmedUpdates = confirmedRequests.stream()
                .map(requestMapper::toRequestDto)
                .toList();

        List<RequestDto> rejectedUpdates = rejectedRequests.stream()
                .map(requestMapper::toRequestDto)
                .toList();

        return new RequestStatusUpdateResult(confirmedUpdates, rejectedUpdates);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Getting requests for event {} by user {}", eventId, userId);

        getUserDtoOrThrow(userId);

        eventInternalClient.getByIdAndState(eventId, null);

        List<Request> requests = requestRepository.findByEventId(eventId);

        return requests.stream()
                .map(requestMapper::toRequestDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestDto> getUserRequests(Long userId) {
        log.info("Getting all requests for user {}", userId);

        getUserDtoOrThrow(userId);

        List<Request> requests = requestRepository.findByRequesterId(userId);

        return requests.stream()
                .map(requestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    private UserDto getUserDtoOrThrow(Long userId) {
        UserDto userDto = userInternalClient.getUserById(userId);

        if (userDto == null) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        return userDto;
    }

}
