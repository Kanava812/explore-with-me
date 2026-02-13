package ru.practicum.event.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import ru.practicum.internal.event.dto.EventShortDto;
import ru.practicum.event.compilation.dto.CreateCompilationDto;
import ru.practicum.event.compilation.dto.ResponseCompilationDto;
import ru.practicum.event.compilation.dto.UpdateCompilationDto;
import ru.practicum.event.compilation.model.Compilation;
import ru.practicum.event.mapper.EventMapper;

import java.util.Set;

@Mapper(componentModel = "spring",
        uses = {EventMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompilationMapper {

    @Mapping(target = "events", source = "events")
    ResponseCompilationDto toCompilationDto(Compilation compilation, Set<EventShortDto> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Compilation toCompilation(CreateCompilationDto requestCompilationDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    void updateCompilationFromDto(UpdateCompilationDto updateCompilationDto, @MappingTarget Compilation compilation);

}
