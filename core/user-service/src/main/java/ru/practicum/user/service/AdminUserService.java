package ru.practicum.user.service;

import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.internal.user.dto.UserDto;

import java.util.List;


public interface AdminUserService {
    UserDto create(NewUserRequest userDto);

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    void delete(Long userId);
}
