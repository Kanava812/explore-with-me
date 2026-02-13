package ru.practicum.request.feignclient.user;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.internal.user.client.UserInternalClient;

@FeignClient(name = "user-service", configuration = UserFeignClientConfig.class , fallback = UserFeignClientFallback.class)
public interface UserFeignClient extends UserInternalClient {
}
