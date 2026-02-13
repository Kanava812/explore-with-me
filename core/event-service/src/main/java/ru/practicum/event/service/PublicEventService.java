package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.internal.event.dto.EventFullDto;
import ru.practicum.internal.event.dto.EventShortDto;
import ru.practicum.event.dto.EventParams;

import java.util.List;
import java.util.Set;

public interface PublicEventService {

    List<EventShortDto> getAllByParams(EventParams eventParams, HttpServletRequest request);

    EventFullDto getById(Long id, Long userId, HttpServletRequest request);

    void likeEvent(Long userId, Long eventId);

    Set<EventShortDto> getRecommendations(Long userId, int maxResults);

}
