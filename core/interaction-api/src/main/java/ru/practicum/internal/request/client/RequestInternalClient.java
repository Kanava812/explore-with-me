package ru.practicum.internal.request.client;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.internal.request.enums.RequestStatus;

import java.util.Map;
import java.util.Set;

public interface RequestInternalClient {

    @GetMapping(path = "/api/v1/internal/events/requests", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<Long, Long> getRequestsCountsByStatusAndEventIds(
            @NotNull @RequestParam RequestStatus status,
            @NotNull @RequestParam Set<@Positive Long> eventIds
    );

    @GetMapping(path = "/api/v1/internal/events/userCheck", produces = MediaType.APPLICATION_JSON_VALUE)
    boolean wasUserAtEvent(@NotNull @RequestParam Long userId,
                           @NotNull @RequestParam Long eventId
    );
}
