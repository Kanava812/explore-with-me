package ru.practicum.event.category.service;

import ru.practicum.event.category.dto.RequestCategoryDto;
import ru.practicum.event.category.dto.ResponseCategoryDto;

import java.util.List;
import java.util.Set;

public interface CategoryService {

    List<ResponseCategoryDto> getCategories(int from, int size);

    ResponseCategoryDto getCategory(long catId);

    ResponseCategoryDto save(RequestCategoryDto categoryDto);

    ResponseCategoryDto update(long catId, RequestCategoryDto categoryDto);

    void delete(long catId);

    List<ResponseCategoryDto> getAllByIds(Set<Long> ids);

}
