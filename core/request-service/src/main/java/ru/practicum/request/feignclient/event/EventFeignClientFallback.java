package ru.practicum.request.feignclient.event;

import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.internal.error.exception.ServiceUnavailableException;
import ru.practicum.internal.event.dto.EventFullDto;
import ru.practicum.internal.event.dto.EventShortDto;
import ru.practicum.internal.event.enums.EventState;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class EventFeignClientFallback implements EventFeignClient {

    @Override
    public EventFullDto getByIdAndState(Long eventId, EventState state) {
        log.warn("EVENT-SERVICE недоступен, fallback вернул исключение ServiceUnavailableException для eventId: {} и state: {}", eventId, state);
        throw new ServiceUnavailableException("EVENT-SERVICE недоступен.");
    }

    @Override
    public List<EventShortDto> getAllByIds(Set<@Positive Long> eventIds) {
        log.warn("EVENT-SERVICE недоступен, fallback вернул пустой список для eventIds: {}", eventIds);
        return List.of();
    }
}
