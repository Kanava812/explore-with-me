package ru.practicum.event.service.util;

import lombok.experimental.UtilityClass;
import ru.practicum.client.RecommendationsClient;
import ru.practicum.event.category.dto.ResponseCategoryDto;
import ru.practicum.event.category.service.CategoryService;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.UserMapper;
import ru.practicum.event.model.Event;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.internal.event.dto.EventFullDto;
import ru.practicum.internal.event.dto.EventShortDto;
import ru.practicum.internal.user.dto.UserDto;
import ru.practicum.internal.user.dto.UserShortDto;
import ru.practicum.internal.user.client.UserInternalClient;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class EventServiceUtil {



    public static Map<Long, ResponseCategoryDto> getResponseCategoryDtoMap(CategoryService categoryService, Set<Long> categoriesIds) {
        return categoryService.getAllByIds(categoriesIds).stream()
                .collect(Collectors.toMap(ResponseCategoryDto::getId, Function.identity()));
    }

    public static Map<Long, UserShortDto> getUserShortDtoMap(UserInternalClient userInternalClient, Set<Long> userIds, UserMapper userMapper) {
        return userInternalClient.getAllByIds(userIds).stream()
                .collect(Collectors.toMap(UserDto::getId, userMapper::toUserShortDto));
    }

    public static List<EventFullDto> getEventFullDtos(
            Map<Long, UserShortDto> userShortDtos,
            Map<Long, ResponseCategoryDto> categoryDtos,
            List<Event> events,
            Map<Long, Long> confirmedRequests,
            Map<Long, Double> ratings,
            EventMapper eventMapper
    ) {
        return events.stream()
                .map(event ->
                        eventMapper.toEventFullDto(
                                event,
                                categoryDtos.get(event.getCategoryId()),
                                userShortDtos.get(event.getInitiatorId()),
                                confirmedRequests.get(event.getId()),
                                ratings.get(event.getId())
                        )
                )
                .toList();
    }

    public static List<EventShortDto> getEventShortDtos(
            Map<Long, UserShortDto> userShortDtos,
            Map<Long, ResponseCategoryDto> categoryDtos,
            List<Event> events,
            Map<Long, Long> confirmedRequests,
            Map<Long, Double> ratings,
            EventMapper eventMapper
    ) {
        return events.stream()
                .map(event ->
                        eventMapper.toEventShortDto(
                                event,
                                categoryDtos.get(event.getCategoryId()),
                                userShortDtos.get(event.getInitiatorId()),
                                confirmedRequests.get(event.getId()),
                                ratings.get(event.getId())
                        )
                )
                .toList();
    }

    public static double getEventRating(Long eventId, RecommendationsClient recommendationsClient) {
        Stream<RecommendedEventProto> interactions = recommendationsClient.getInteractionsCount(eventId);

        return interactions
                .filter(proto -> proto.getEventId() == eventId)
                .findFirst()
                .map(RecommendedEventProto::getScore)
                .orElse(0.0);
    }

    public static Map<Long, Double> getEventRatings(Set<Long> eventIds, RecommendationsClient recommendationsClient) {
        Stream<RecommendedEventProto> interactions = recommendationsClient.getInteractionsCount(eventIds);
        return interactions
                .collect(Collectors.toMap(
                        RecommendedEventProto::getEventId,
                        RecommendedEventProto::getScore,
                        (existing, replacement) -> existing));
    }
}


