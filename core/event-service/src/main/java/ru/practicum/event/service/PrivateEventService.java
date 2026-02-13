package ru.practicum.event.service;

import ru.practicum.internal.event.dto.EventFullDto;
import ru.practicum.internal.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventRequest;

import java.util.List;

public interface PrivateEventService {

    EventFullDto create(Long userId, NewEventDto newEventDto);

    EventFullDto update(Long userId, Long eventId, UpdateEventRequest request);

    EventFullDto getById(Long userId, Long eventId);

    List<EventShortDto> getAll(Long userId, int from, int size);

}
