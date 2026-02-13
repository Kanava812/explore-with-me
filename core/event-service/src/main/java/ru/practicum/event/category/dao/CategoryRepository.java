package ru.practicum.event.category.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.event.category.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
