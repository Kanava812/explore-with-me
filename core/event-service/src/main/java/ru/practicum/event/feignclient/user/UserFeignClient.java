package ru.practicum.event.feignclient.user;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.internal.user.client.UserInternalClient;

@FeignClient(name = "user-service", configuration = UserFeignConfig.class , fallback = UserFeignClientFallback.class)
public interface UserFeignClient extends UserInternalClient {
}
