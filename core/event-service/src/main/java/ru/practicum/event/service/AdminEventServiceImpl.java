package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.RecommendationsClient;
import ru.practicum.internal.error.exception.BadRequestException;
import ru.practicum.internal.error.exception.NotFoundException;
import ru.practicum.internal.error.exception.RuleViolationException;
import ru.practicum.internal.event.dto.EventFullDto;
import ru.practicum.internal.event.enums.EventState;
import ru.practicum.internal.request.enums.RequestStatus;
import ru.practicum.internal.request.client.RequestInternalClient;
import ru.practicum.internal.user.dto.UserShortDto;
import ru.practicum.internal.user.client.UserInternalClient;
import ru.practicum.event.category.dto.ResponseCategoryDto;
import ru.practicum.event.category.service.CategoryService;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.dao.EventSpecifications;
import ru.practicum.event.dto.AdminEventDto;
import ru.practicum.event.dto.UpdateEventRequest;
import ru.practicum.event.enums.StateAction;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.UserMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.util.EventServiceUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminEventServiceImpl implements AdminEventService {

    private final EventRepository eventRepository;
    private final RecommendationsClient recommendationsClient;
    private final UserInternalClient userInternalClient;
    private final CategoryService categoryService;
    private final RequestInternalClient requestInternalClient;

    private final EventMapper eventMapper;
    private final UserMapper userMapper;

    @Override
    public EventFullDto update(Long eventId, UpdateEventRequest updateEventRequest) throws RuleViolationException {
        log.info("Администратором обновляется событие c ID {}: {}", eventId, updateEventRequest);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));

        validateCriticalRules(updateEventRequest, event);

        ResponseCategoryDto categoryDto = categoryService.getCategory(event.getCategoryId());

        eventMapper.updateEvent(event, updateEventRequest, categoryDto.getId());

        if (Objects.equals(updateEventRequest.getStateAction(), StateAction.PUBLISH_EVENT)) {
            event.setPublishedOn(LocalDateTime.now());
        }

        eventRepository.save(event);

        Long confirmedRequests = requestInternalClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED,
                Set.of(eventId)).getOrDefault(eventId, 0L);

        UserShortDto userShortDto = userMapper.toUserShortDto(userInternalClient.getUserById(event.getInitiatorId()));

        if (event.getPublishedOn() == null) {
            return eventMapper.toEventFullDto(event, categoryDto, userShortDto, confirmedRequests, 0.0);
        }

        log.info("Администратором обновлено событие c ID {}.", event.getId());

        return eventMapper.toEventFullDto(event, categoryDto, userShortDto, confirmedRequests,
                EventServiceUtil.getEventRating(eventId, recommendationsClient));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getAllByParams(AdminEventDto adminEventDto) {
        log.info("Получение администратором событий по параметрам: {}", adminEventDto);

        List<Event> events = eventRepository.findAll(
                EventSpecifications.adminSpecification(adminEventDto),
                makePageable(adminEventDto)
        ).getContent();

        Set<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toSet());

        Map<Long, Long> confirmedRequests = requestInternalClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, eventIds);

        Map<Long, Double> ratings = EventServiceUtil.getEventRatings(eventIds, recommendationsClient);

        Set<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());

        Set<Long> categoriesIds = events.stream()
                .map(Event::getCategoryId)
                .collect(Collectors.toSet());

        Map<Long, UserShortDto> userShortDtos = EventServiceUtil.getUserShortDtoMap(userInternalClient, userIds, userMapper);
        Map<Long, ResponseCategoryDto> categoryDtos = EventServiceUtil.getResponseCategoryDtoMap(categoryService, categoriesIds);

        return EventServiceUtil.getEventFullDtos(userShortDtos, categoryDtos, events, confirmedRequests, ratings, eventMapper);
    }

    private static Pageable makePageable(AdminEventDto adminEventDto) {
        return PageRequest.of(
                adminEventDto.getFrom().intValue() / adminEventDto.getSize().intValue(),
                adminEventDto.getSize().intValue()
        );
    }

    private static void validateCriticalRules(UpdateEventRequest updateEventRequest, Event event) {
        if (updateEventRequest.getEventDate() != null) {
            if (LocalDateTime.now().plusHours(1).isAfter(updateEventRequest.getEventDate())) {
                throw new BadRequestException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
            }
        }

        if (Objects.equals(updateEventRequest.getStateAction(), StateAction.REJECT_EVENT)) {
            if (Objects.equals(event.getState(), EventState.PUBLISHED)) {
                throw new RuleViolationException("Событие нельзя отклонить, если оно опубликовано (PUBLISHED)");
            }
        } else if (Objects.equals(updateEventRequest.getStateAction(), StateAction.PUBLISH_EVENT)) {
            if (!Objects.equals(event.getState(), EventState.PENDING)) {
                throw new RuleViolationException("Событие должно находиться в статусе PENDING");
            }
        }
    }
}
