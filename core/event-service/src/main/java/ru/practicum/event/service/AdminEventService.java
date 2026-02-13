package ru.practicum.event.service;

import ru.practicum.event.dto.AdminEventDto;
import ru.practicum.internal.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventRequest;

import java.util.List;

public interface AdminEventService {

    EventFullDto update(Long eventId, UpdateEventRequest updateEventRequest);

    List<EventFullDto> getAllByParams(AdminEventDto adminEventDto);
}
