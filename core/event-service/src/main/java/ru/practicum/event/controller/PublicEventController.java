package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.internal.event.dto.EventFullDto;
import ru.practicum.internal.event.dto.EventShortDto;
import ru.practicum.internal.event.client.EventInternalClient;
import ru.practicum.event.dto.EventParams;
import ru.practicum.event.enums.EventsSort;
import ru.practicum.event.service.PublicEventService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@Validated
@RequiredArgsConstructor
public class PublicEventController {

    private final PublicEventService publicEventService;

    @GetMapping(path = EventInternalClient.URL)
    public List<EventShortDto> getAllByParams(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(defaultValue = "EVENT_DATE") EventsSort eventsSort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        EventParams params = EventParams.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .eventsSort(eventsSort)
                .from(from)
                .size(size)
                .build();
        return publicEventService.getAllByParams(params, request);
    }

    @GetMapping(path = EventInternalClient.URL + "/{eventId}")
    public EventFullDto getById(
            @PathVariable @Positive Long eventId,
            @RequestHeader("X-EWM-USER-ID") Long userId,
            HttpServletRequest request
    ) {
        return publicEventService.getById(eventId, userId, request);
    }

    @GetMapping("/recommendations")
    Set<EventShortDto> getRecommendations(@RequestHeader("X-EWM-USER-ID") Long userId,
                                          @RequestParam(defaultValue = "10") int maxResults) {
        return publicEventService.getRecommendations(userId, maxResults);
    }

    @PutMapping("/{eventId}/like")
    public void likeEvent(@RequestHeader("X-EWM-USER-ID") Long userId,
                                @PathVariable @Positive Long eventId) {
        publicEventService.likeEvent(userId, eventId);
    }
}
