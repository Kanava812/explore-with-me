package ru.practicum.user.service;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.internal.user.dto.UserDto;
import ru.practicum.user.dao.UserRepository;
import ru.practicum.user.mapper.UserMapper;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserInternalServiceImpl implements UserInternalService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @Override
    public UserDto getUserById(Long userId) {
        log.info("Получение пользователя по id: {}", userId);
        return userRepository.findById(userId).map(userMapper::toUserDto)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    @Override
    public List<UserDto> getAllByIds(Set<Long> userIds) {
        return userRepository.findAllById(userIds).stream()
                .map(userMapper::toUserDto)
                .toList();
    }

}
