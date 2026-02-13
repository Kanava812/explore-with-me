package ru.practicum.analyzer.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "user_action")
@EqualsAndHashCode(of = "id")
public class UserAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
}
