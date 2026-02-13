package ru.practicum.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.internal.request.dto.RequestDto;
import ru.practicum.request.model.Request;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(source = "request.eventId", target = "event")
    @Mapping(source = "request.requesterId", target = "requester")
    @Mapping(source = "status", target = "status")
    RequestDto toRequestDto(Request request);

}
