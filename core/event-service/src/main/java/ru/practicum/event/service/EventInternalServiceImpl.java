package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.client.RecommendationsClient;
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
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.UserMapper;
import ru.practicum.event.service.util.EventServiceUtil;
import ru.practicum.event.model.Event;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventInternalServiceImpl implements EventInternalService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final RequestInternalClient requestInternalClient;
    private final CategoryService categoryService;
    private final UserMapper userMapper;
    private final UserInternalClient userInternalClient;
    private final RecommendationsClient recommendationsClient;

    @Override
    public EventFullDto getByIdAndState(Long eventId, EventState state) {
        log.debug("Получен запрос на получение события с ID = {} и state = {}", eventId, state);

        Event event;
        if (state == null) {
            event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new NotFoundException("Событие c ID = " + eventId + " не найдено"));
        } else {
            event = eventRepository.findByIdAndState(eventId, state)
                    .orElseThrow(() -> new NotFoundException("Событие c ID = " + eventId + " не найдено"));
        }

        Long confirmedRequests = requestInternalClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, Set.of(eventId)).getOrDefault(eventId, 0L);

        ResponseCategoryDto categoryDto = categoryService.getCategory(event.getCategoryId());

        UserShortDto userDto = userMapper.toUserShortDto(userInternalClient.getUserById(event.getInitiatorId()));

        if (event.getPublishedOn() == null) {
            return eventMapper.toEventFullDto(event, categoryDto, userDto, confirmedRequests, 0.0);
        }

        Double rating = EventServiceUtil.getEventRating(eventId, recommendationsClient);

        EventFullDto dto = eventMapper.toEventFullDto(event, categoryDto, userDto, confirmedRequests, rating);

        log.debug("Получено событие с ID={}: {}", eventId, dto);

        return dto;
    }

    @Override
    public List<EventShortDto> getAllByIds(Set<Long> eventIds) {
        log.debug("Получен запрос на получение событий с IDs = {}", eventIds);

        List<Event> events = eventRepository.findAllById(eventIds);

        if (events.isEmpty()) {
            log.warn("Нет событий по указанным IDs {}", eventIds);
            return Collections.emptyList();
        }

        Set<Long> dbEventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toSet());

        Map<Long, Long> confirmedRequests = requestInternalClient.getRequestsCountsByStatusAndEventIds(RequestStatus.CONFIRMED, dbEventIds);

        Map<Long, Double> ratings = EventServiceUtil.getEventRatings(dbEventIds, recommendationsClient);

        Set<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());
        Set<Long> categoryIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());

        Map<Long, UserShortDto> userShortDtos = EventServiceUtil.getUserShortDtoMap(userInternalClient, userIds, userMapper);
        Map<Long, ResponseCategoryDto> categoryDtos = EventServiceUtil.getResponseCategoryDtoMap(categoryService, categoryIds);

        return EventServiceUtil.getEventShortDtos(userShortDtos, categoryDtos, events, confirmedRequests, ratings, eventMapper);
    }
}

