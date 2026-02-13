package ru.practicum.event.feignclient.request;

import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.internal.request.enums.RequestStatus;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RequestFeignlClientFallback implements RequestFeignClient {
    @Override
    public Map<Long, Long> getRequestsCountsByStatusAndEventIds(RequestStatus status, Set<@Positive Long> eventIds) {
        log.warn("REQUEST-SERVICE недоступен, fallback отдал пустую мапу.");
        return eventIds.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        e -> 0L
                ));
    }

    @Override
    public boolean wasUserAtEvent(Long userId, Long eventId){
        log.warn("REQUEST-SERVICE недоступен, fallback передал false.");
        return false;
    }
}