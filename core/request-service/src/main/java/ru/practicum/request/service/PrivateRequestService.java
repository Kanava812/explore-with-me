package ru.practicum.request.service;

import jakarta.validation.Valid;
import ru.practicum.internal.request.dto.RequestDto;
import ru.practicum.request.dto.RequestStatusUpdate;
import ru.practicum.request.dto.RequestStatusUpdateResult;

import java.util.List;

public interface PrivateRequestService {

    RequestDto createRequest(Long userId, Long eventId);

    RequestDto cancelRequest(Long userId, Long requestId);

    List<RequestDto> getEventRequests(Long userId, Long eventId);

    RequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, @Valid RequestStatusUpdate updateRequest);

    List<RequestDto> getUserRequests(Long userId);
}
