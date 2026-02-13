package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.analyzer.entity.EventSimilarity;


import java.util.List;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {
    List<EventSimilarity> findByEventAOrEventB(Long eventA, Long eventB);

    List<EventSimilarity> findAllByEventAInOrEventBIn(List<Long> eventAids, List<Long> eventBids);
}