package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.internal.user.dto.UserDto;
import ru.practicum.internal.user.client.UserInternalClient;
import ru.practicum.user.service.UserInternalService;

import java.util.List;
import java.util.Set;

@RestController
@Validated
@RequiredArgsConstructor
public class UserInternalController implements UserInternalClient {

    private final UserInternalService userInternalService;

    @Override
    public UserDto getUserById(Long userId) {
        return userInternalService.getUserById(userId);
    }

    @Override
    public List<UserDto> getAllByIds(Set<Long> userIds) {
        return userInternalService.getAllByIds(userIds);
    }
}
