package ru.practicum.internal.event.client;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.internal.event.dto.EventFullDto;
import ru.practicum.internal.event.dto.EventShortDto;
import ru.practicum.internal.event.enums.EventState;

import java.util.List;
import java.util.Set;

public interface EventInternalClient {
    String URL = "/events";

    @GetMapping(path = URL + "/{eventId}/state", produces = MediaType.APPLICATION_JSON_VALUE)
    EventFullDto getByIdAndState(@PathVariable @Positive Long eventId, @RequestParam @Nullable EventState state);

    @GetMapping(path = URL + "/ids", produces = MediaType.APPLICATION_JSON_VALUE)
    List<EventShortDto> getAllByIds(@RequestParam @NotNull Set<@Positive Long> eventIds);
}
