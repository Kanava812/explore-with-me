package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.RecommendationsClient;
import ru.practicum.client.UserActionClient;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.internal.error.exception.BadRequestException;
import ru.practicum.internal.error.exception.NotFoundException;
import ru.practicum.internal.event.dto.EventFullDto;
import ru.practicum.internal.event.dto.EventShortDto;
import ru.practicum.internal.event.enums.EventState;
import ru.practicum.internal.request.enums.RequestStatus;
import ru.practicum.internal.request.client.RequestInternalClient;
import ru.practicum.internal.user.dto.UserShortDto;
import ru.practicum.internal.user.client.UserInternalClient;
import ru.practicum.event.category.dto.ResponseCategoryDto;
import ru.practicum.event.category.service.CategoryService;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.dao.EventSpecifications;
import ru.practicum.event.dto.EventParams;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.UserMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.util.EventServiceUtil;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventServiceImpl implements PublicEventService {

    private final EventRepository eventRepository;
    private final UserInternalClient userInternalClient;
    private final CategoryService categoryService;
    private final RequestInternalClient requestInternalClient;
    private final UserActionClient userActionClient;
    private final RecommendationsClient recommendationsClient;
    private final EventMapper eventMapper;
    private final UserMapper userMapper;

    @Override
    public List<EventShortDto> getAllByParams(EventParams params, HttpServletRequest request) {
        log.info("Получение событий с параметрами: {}", params.toString());

        if (params.getRangeStart() != null && params.getRangeEnd() != null && params.getRangeEnd().isBefore(params.getRangeStart())) {
            log.error("Ошибка в параметрах диапазона дат: start={}, end={}", params.getRangeStart(), params.getRangeEnd());
            throw new BadRequestException("Дата начала должна быть раньше даты окончания");
        }

        if (params.getRangeStart() == null) params.setRangeStart(LocalDateTime.now());

        List<Event> events = eventRepository
                .findAll(EventSpecifications.publicSpecification(params), makePageable(params))
                .stream()
                .toList();

        Set<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());

        Map<Long, Long> confirmedRequests = requestInternalClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, eventIds);

        if (params.getOnlyAvailable()) {
            events = events.stream()
                    .filter(event -> event.getParticipantLimit() > confirmedRequests.get(event.getId()))
                    .toList();
        }

        if (events.isEmpty()) {
            log.warn("Нет свободных событий по указанным параметрам {}", params);
            return Collections.emptyList();
        }

        Map<Long, Double> ratings = EventServiceUtil.getEventRatings(eventIds, recommendationsClient);

        Set<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());
        Set<Long> categoriesIds = new HashSet<>(params.getCategories());

        Map<Long, UserShortDto> userShortDtos = EventServiceUtil.getUserShortDtoMap(userInternalClient, userIds, userMapper);
        Map<Long, ResponseCategoryDto> categoryDtos = EventServiceUtil.getResponseCategoryDtoMap(categoryService, categoriesIds);

        return EventServiceUtil.getEventShortDtos(userShortDtos, categoryDtos, events, confirmedRequests, ratings, eventMapper);
    }

    @Override
    public EventFullDto getById(Long eventId, Long userId, HttpServletRequest request) {
        log.debug("Получение события с ID = {}", eventId);

        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие не найдено."));

        Long confirmedRequests = requestInternalClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, Set.of(eventId)).getOrDefault(eventId, 0L);

        ResponseCategoryDto categoryDto = categoryService.getCategory(event.getCategoryId());

        UserShortDto userShortDto = userMapper.toUserShortDto(userInternalClient.getUserById(event.getInitiatorId()));

        userActionClient.sendUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW, Instant.now());

        if (event.getPublishedOn() == null) {
            return eventMapper.toEventFullDto(event, categoryDto, userShortDto, confirmedRequests, 0.0);
        }

        Double rating = EventServiceUtil.getEventRating(eventId, recommendationsClient);

        EventFullDto dto = eventMapper.toEventFullDto(event, categoryDto, userShortDto, confirmedRequests, rating);

        log.debug("Получено событие с ID={}: {}", eventId, dto);

        return dto;
    }

    private Pageable makePageable(EventParams params) {
        Sort sort = params.getEventsSort().getSort();
        return PageRequest.of(params.getFrom() / params.getSize(), params.getSize(), sort);
    }

    public void likeEvent(Long userId, Long eventId) {
        if (!requestInternalClient.wasUserAtEvent(userId, eventId)) {
            throw new BadRequestException("Пользователь может лайкать только посещённые им мероприятия.");
        }
        userActionClient.sendUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE, Instant.now());
    }

    public Set<EventShortDto> getRecommendations(Long userId, int maxResults) {
        Stream<RecommendedEventProto> stream = recommendationsClient.getRecommendationsForUser(userId, maxResults);
        Set<Long> eventIds = stream.map(RecommendedEventProto::getEventId)
                .collect(Collectors.toSet());

        Set<Event> events = eventRepository.findAllByIdIn(eventIds);


        return events.stream()
                .map(event -> eventMapper.toEventShortDto(event, null, null, null, null))
                .collect(Collectors.toSet());
    }
}