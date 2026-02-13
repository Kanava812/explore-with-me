package ru.practicum.event.feignclient.request;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.internal.request.client.RequestInternalClient;

@FeignClient(name = "request-service", configuration = RequestFeignClientErrorDecoder.class, fallback = RequestFeignlClientFallback.class)
public interface RequestFeignClient extends RequestInternalClient {
}
