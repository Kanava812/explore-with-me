package ru.practicum.event.feignclient.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.internal.error.exception.ServiceUnavailableException;
import ru.practicum.internal.user.dto.UserDto;

import java.util.List;
import java.util.Set;


@Slf4j
@Component
public class UserFeignClientFallback implements UserFeignClient {

    @Override
    public UserDto getUserById(Long userId) {
        log.warn("USER-SERVICE недоступен, fallback вернул исключение ServiceUnavailableException для id: {}", userId);
        throw new ServiceUnavailableException("USER-SERVICE  недоступен.");
    }

    @Override
    public List<UserDto> getAllByIds(Set<Long> userIds) {
        log.warn("USER-SERVICE  недоступен, fallback вернул пустой список для ids: {}", userIds);
        return List.of();
    }
}