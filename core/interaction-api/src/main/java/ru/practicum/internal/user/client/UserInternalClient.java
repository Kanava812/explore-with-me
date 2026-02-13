package ru.practicum.internal.user.client;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.internal.user.dto.UserDto;

import java.util.List;
import java.util.Set;

public interface UserInternalClient {
    String URL = "/api/v1/internal/users";

    @GetMapping(path = URL + "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    UserDto getUserById(@Positive @PathVariable Long userId);

    @GetMapping(path = URL, produces = MediaType.APPLICATION_JSON_VALUE)
    List<UserDto> getAllByIds(@NotNull @RequestParam Set<@Positive Long> userIds);

}
