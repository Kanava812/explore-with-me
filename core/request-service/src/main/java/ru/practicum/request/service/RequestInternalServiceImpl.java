package ru.practicum.request.service;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.internal.request.enums.RequestStatus;
import ru.practicum.request.dao.RequestRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RequestInternalServiceImpl implements RequestInternalService {

    private final RequestRepository requestRepository;

    @Override
    public Map<Long, Long> getRequestsCountsByStatusAndEventIds(RequestStatus status, Set<@Positive Long> eventIds) {
        log.info("Getting requests counts for status {} and eventIds {}", status, eventIds);

        List<Object[]> requestsCounts = requestRepository.getRequestsCountsByStatusAndEventIds(status, eventIds);

        return requestsCounts.stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));
    }

    public boolean wasUserAtEvent(Long userId, Long eventId) {
        log.info("Checking participation of user with id {} in event with id {}", userId, eventId);
        return requestRepository.existsByRequesterIdAndEventId(userId, eventId);
    }
}

