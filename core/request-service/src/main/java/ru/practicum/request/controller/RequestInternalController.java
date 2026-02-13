package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.internal.request.enums.RequestStatus;
import ru.practicum.internal.request.client.RequestInternalClient;
import ru.practicum.request.service.RequestInternalService;

import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class RequestInternalController implements RequestInternalClient {
    private final RequestInternalService requestService;

    @Override
    public Map<Long, Long> getRequestsCountsByStatusAndEventIds(RequestStatus status, Set<Long> eventIds) {
        return requestService.getRequestsCountsByStatusAndEventIds(status, eventIds);
    }

    @Override
    public boolean wasUserAtEvent(Long userId, Long eventId) {
        return requestService.wasUserAtEvent(userId, eventId);
    }
}
