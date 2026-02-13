package ru.practicum.event.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.internal.error.exception.NotFoundException;
import ru.practicum.internal.event.dto.EventShortDto;
import ru.practicum.event.compilation.dao.CompilationRepository;
import ru.practicum.event.compilation.dto.CreateCompilationDto;
import ru.practicum.event.compilation.dto.ResponseCompilationDto;
import ru.practicum.event.compilation.dto.UpdateCompilationDto;
import ru.practicum.event.compilation.mapper.CompilationMapper;
import ru.practicum.event.compilation.model.Compilation;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.EventInternalService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;
    private final EventInternalService eventService;

    /**
     * === Public endpoints accessible to all users. ===
     */

    @Override
    public List<ResponseCompilationDto> getCompilations(Boolean pinned, int from, int size) {
        log.info("Get compilations with pinned={} from={} size={}", pinned, from, size);
        Pageable pageable = PageRequest.of(from / size, size);

        List<Compilation> compilations;

        if (pinned == null) {
            compilations = compilationRepository
                    .findAll(pageable)
                    .toList();
        } else {
            compilations = compilationRepository
                    .findAllByPinned(pinned, pageable)
                    .toList();
        }

        Set<Event> events = compilations.stream()
                .map(Compilation::getEvents)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        if (events.isEmpty()) {
            return compilations.stream()
                    .map(compilation -> compilationMapper.toCompilationDto(compilation, Collections.emptySet()))
                    .collect(Collectors.toList());
        }

        Set<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());

        List<EventShortDto> eventDtos = eventService.getAllByIds(eventIds);

        Map<Long, EventShortDto> eventDtoMap = eventDtos.stream()
                .collect(Collectors.toMap(EventShortDto::getId, e -> e));

        return compilations.stream()
                .map(c -> {
                    Set<EventShortDto> compilationEventDtos = c.getEvents().stream()
                            .map(eventDtoMap::get)
                            .collect(Collectors.toSet());
                    return compilationMapper.toCompilationDto(c, compilationEventDtos);
                })
                .toList();
    }

    @Override
    public ResponseCompilationDto getCompilation(long compId) {
        log.info("Get compilation with id={}", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        if (compilation.getEvents().isEmpty()) {
            return compilationMapper.toCompilationDto(compilation, Collections.emptySet());
        }
        Set<Long> eventIds = compilation.getEvents().stream().map(Event::getId).collect(Collectors.toSet());
        Set<EventShortDto> eventShortDtos = new HashSet<>(eventService.getAllByIds(eventIds));
        return compilationMapper.toCompilationDto(compilation, eventShortDtos);
    }

    /**
     * === Admin endpoints accessible only for admins. ===
     */

    @Override
    @Transactional
    public ResponseCompilationDto save(CreateCompilationDto requestCompilationDto) {
        log.info("Save compilation {}", requestCompilationDto);
        Compilation newCompilation = compilationMapper.toCompilation(requestCompilationDto);

        if (requestCompilationDto.getEvents() == null || requestCompilationDto.getEvents().isEmpty()) {
            Compilation saved = compilationRepository.save(newCompilation);
            return compilationMapper.toCompilationDto(saved, Collections.emptySet());
        }

        Set<Event> events = eventRepository.findAllByIdIn(requestCompilationDto.getEvents());

        newCompilation.setEvents(events);

        Compilation saved = compilationRepository.saveAndFlush(newCompilation);

        Set<EventShortDto> eventShortDtos = new HashSet<>(eventService.getAllByIds(requestCompilationDto.getEvents()));

        return compilationMapper.toCompilationDto(saved, eventShortDtos);
    }

    @Override
    @Transactional
    public ResponseCompilationDto update(long compId, UpdateCompilationDto updateCompilationDto) {
        Compilation fromDb = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        compilationMapper.updateCompilationFromDto(updateCompilationDto, fromDb);

        if (updateCompilationDto.getEvents() == null || updateCompilationDto.getEvents().isEmpty()) {
            Compilation updated = compilationRepository.save(fromDb);
            return compilationMapper.toCompilationDto(updated, Collections.emptySet());
        }

        Set<Event> events = eventRepository.findAllByIdIn(updateCompilationDto.getEvents());

        fromDb.setEvents(events);

        Compilation updated = compilationRepository.save(fromDb);


        Set<EventShortDto> eventShortDtos = new HashSet<>(eventService.getAllByIds(updateCompilationDto.getEvents()));

        return compilationMapper.toCompilationDto(updated, eventShortDtos);
    }

    @Override
    @Transactional
    public void delete(long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }

        compilationRepository.deleteById(compId);
    }

}
