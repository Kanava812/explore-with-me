package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.entity.EventSimilarity;
import ru.practicum.analyzer.entity.UserAction;
import ru.practicum.analyzer.repository.EventSimilarityRepository;
import ru.practicum.analyzer.repository.UserInteractionRepository;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final EventSimilarityRepository similarityRepository;
    private final UserInteractionRepository interactionRepository;

    @Transactional
    public void handleSimilarity(EventSimilarityAvro avro) {
        log.info("Processing similarity event: ");
        EventSimilarity similarity = new EventSimilarity();
        similarity.setEventA(avro.getEventA());
        similarity.setEventB(avro.getEventB());
        similarity.setScore(avro.getScore());
        similarity.setUpdatedAt(avro.getTimestamp());
        similarityRepository.save(similarity);
    }

    @Transactional
    public void handleUserAction(UserActionAvro avro) {
        log.info("Processing user action: userId={}, eventId={}, actionType={}, timestamp={}",
                avro.getUserId(), avro.getEventId(), avro.getActionType(), avro.getTimestamp());
        UserAction action = new UserAction();
        action.setUserId(avro.getUserId());
        action.setEventId(avro.getEventId());
        action.setActionType(avro.getActionType().toString());
        double newWeight = getWeightFromActionType(avro.getActionType());

        if (Objects.isNull(action.getWeight()) || newWeight > action.getWeight()) {
            action.setWeight(newWeight);
            action.setTimestamp(avro.getTimestamp());
            interactionRepository.save(action);
            log.info("Saved user action: userId={}, eventId={}, weight={}, actionType={}",
                    action.getUserId(), action.getEventId(), action.getWeight(), action.getActionType());
        } else {
            log.info("Skipped saving user action: userId={}, eventId={}, newWeight={} <= currentWeight={}",
                    action.getUserId(), action.getEventId(), newWeight, action.getWeight());
        }
    }

    private double getWeightFromActionType(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
            default -> throw new IllegalArgumentException("Неизвестный тип действия: " + actionType);
        };
    }
}
