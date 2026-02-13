package ru.practicum.event.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.internal.event.dto.EventFullDto;
import ru.practicum.internal.event.dto.EventShortDto;
import ru.practicum.internal.event.enums.EventState;
import ru.practicum.internal.event.client.EventInternalClient;
import ru.practicum.event.service.EventInternalService;

import java.util.List;
import java.util.Set;


@RestController
@AllArgsConstructor
@Slf4j
public class EventInternalController implements EventInternalClient {
    private final EventInternalService eventService;

    @Override
    public EventFullDto getByIdAndState(Long eventId, EventState state) {
        return eventService.getByIdAndState(eventId, state);
    }

    @Override
    public List<EventShortDto> getAllByIds(Set<Long> eventIds) {
        return eventService.getAllByIds(eventIds);
    }
}