package ru.practicum.event.compilation.service;



import ru.practicum.event.compilation.dto.CreateCompilationDto;
import ru.practicum.event.compilation.dto.ResponseCompilationDto;
import ru.practicum.event.compilation.dto.UpdateCompilationDto;

import java.util.List;

public interface CompilationService {

    List<ResponseCompilationDto> getCompilations(Boolean pinned, int from, int size);

    ResponseCompilationDto getCompilation(long compId);

    ResponseCompilationDto save(CreateCompilationDto requestCompilationDto);

    ResponseCompilationDto update(long compId, UpdateCompilationDto updateCompilationDto);

    void delete(long compId);

}
