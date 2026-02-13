package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.analyzer.entity.UserAction;


import java.util.List;

public interface UserInteractionRepository extends JpaRepository<UserAction, Long> {

    List<UserAction> findByUserIdOrderByTimestampDesc(Long userId);

    List<UserAction> findAllByEventIdIn(List<Long> eventIds);

}