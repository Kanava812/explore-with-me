package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import ru.practicum.internal.user.dto.UserDto;
import ru.practicum.internal.user.dto.UserShortDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserShortDto toUserShortDto(UserDto user);

}
