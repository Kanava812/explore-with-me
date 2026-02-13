package ru.practicum.request.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.internal.request.enums.RequestStatus;
import ru.practicum.request.model.Request;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RequestRepository extends JpaRepository<Request, Long> {
    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findByRequesterId(Long requesterId);

    List<Request> findByEventId(Long eventId);

    Optional<Request> findByIdAndRequesterId(Long id, Long requesterId);

    List<Request> findByEventIdAndStatus(Long eventId, RequestStatus status);

    Optional<Request> findByIdAndEventId(Long id, Long eventId);

    @Query("SELECT r.eventId, COUNT(r) " +
            "FROM Request r " +
            "WHERE r.eventId IN :eventIds AND r.status = :status " +
            "GROUP BY r.eventId")
    List<Object[]> getRequestsCountsByStatusAndEventIds(@Param("status") RequestStatus status, @Param("eventIds") Set<Long> eventIds);
}
