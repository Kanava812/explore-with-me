package ru.practicum.request.feignclient.event;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.internal.event.client.EventInternalClient;

@FeignClient(name = "event-service", configuration = EventFeignConfig.class, fallback = EventFeignClientFallback.class)
public interface EventFeignClient extends EventInternalClient {
}
