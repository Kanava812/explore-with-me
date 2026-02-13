package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.analyzer.entity.EventSimilarity;
import ru.practicum.analyzer.entity.UserAction;
import ru.practicum.analyzer.exception.InteractionCalculationException;
import ru.practicum.analyzer.repository.EventSimilarityRepository;
import ru.practicum.analyzer.repository.UserInteractionRepository;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;



@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final EventSimilarityRepository similarityRepository;
    private final UserInteractionRepository interactionRepository;

    public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, long maxResults) {
        List<UserAction> userActions = interactionRepository.findByUserIdOrderByTimestampDesc(userId);
        if (userActions.isEmpty()) {
            return Stream.empty();
        }

        List<Long> interactedEventIds = userActions.stream()
                .map(UserAction::getEventId)
                .limit(10)
                .toList();

        List<EventSimilarity> allSimilarities = similarityRepository.findAllByEventAInOrEventBIn(interactedEventIds, interactedEventIds);

        Map<Long, List<EventSimilarity>> similaritiesByEvent = allSimilarities.stream()
                .collect(Collectors.groupingBy(similarity -> {
                    Long otherEventId = similarity.getEventA().equals(similarity.getEventB()) ? null :
                            similarity.getEventA();
                    return otherEventId != null && interactedEventIds.contains(otherEventId) ? similarity.getEventB() : similarity.getEventA();
                }));

        Set<Long> candidateEventIds = similaritiesByEvent.keySet().stream()
                .filter(eventId -> !interactedEventIds.contains(eventId))
                .collect(Collectors.toSet());

        return candidateEventIds.stream()
                .map(eventId -> {
                    List<EventSimilarity> neighbors = similaritiesByEvent.getOrDefault(eventId, Collections.emptyList());

                    double weightedSum = 0.0;
                    double similaritySum = 0.0;
                    for (EventSimilarity neighbor : neighbors) {
                        long neighborEventId = neighbor.getEventA().equals(eventId) ? neighbor.getEventB() : neighbor.getEventA();
                        Optional<UserAction> actionOpt = userActions.stream()
                                .filter(a -> a.getEventId().equals(neighborEventId))
                                .findFirst();
                        if (actionOpt.isPresent()) {
                            UserAction action = actionOpt.get();
                            weightedSum += neighbor.getScore() * action.getWeight();
                            similaritySum += neighbor.getScore();
                        }
                    }

                    double score = similaritySum > 0 ? weightedSum / similaritySum : 0.0;

                    return RecommendedEventProto.newBuilder()
                            .setEventId(eventId)
                            .setScore(score)
                            .build();
                })
                .sorted(Comparator.comparingDouble(RecommendedEventProto::getScore).reversed())
                .limit(maxResults);
    }

    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, long maxResults) {
        List<EventSimilarity> similarities = similarityRepository.findByEventAOrEventB(eventId, eventId);

        List<Long> interactedEventIds = interactionRepository.findByUserIdOrderByTimestampDesc(userId)
                .stream()
                .map(UserAction::getEventId)
                .toList();

        return similarities.stream()
                .flatMap(s -> {
                    long otherEventId = s.getEventA().equals(eventId) ? s.getEventB() : s.getEventA();
                    if (!interactedEventIds.contains(otherEventId)) {
                        return Stream.of(RecommendedEventProto.newBuilder()
                                .setEventId(otherEventId)
                                .setScore(s.getScore())
                                .build());
                    }
                    return Stream.empty();
                })
                .sorted(Comparator.comparingDouble(RecommendedEventProto::getScore).reversed())
                .limit(maxResults);
    }


    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        log.info("Calculating interactions weight for eventIds={}", eventIds);
        try {
            if (Objects.isNull(eventIds) || eventIds.isEmpty()) {
                log.warn("Empty eventIds list provided");
                return Stream.empty();
            }

            // Получение всех взаимодействий сразу
            List<UserAction> allInteractions = interactionRepository.findAllByEventIdIn(eventIds);

            // Картографируем все события с группами пользователей и их весами
            Map<Long, Map<Long, Double>> groupedInteractions = allInteractions.stream()
                    .collect(Collectors.groupingBy(
                            UserAction::getEventId,
                            Collectors.groupingBy(
                                    UserAction::getUserId,
                                    Collectors.collectingAndThen(
                                            Collectors.maxBy(Comparator.comparing(UserAction::getWeight)),
                                            opt -> opt.map(UserAction::getWeight).orElse(0.0)
                                    )
                            )
                    ));

            // Составляем итоговый поток
            return eventIds.stream()
                    .distinct()
                    .map(eventId -> {
                        double totalWeight = groupedInteractions.getOrDefault(eventId, Collections.emptyMap()).values().stream()
                                .mapToDouble(Double::doubleValue)
                                .sum();

                        return RecommendedEventProto.newBuilder()
                                .setEventId(eventId)
                                .setScore(totalWeight)
                                .build();
                    }).sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed());
        } catch (Exception e) {
            log.error("Error calculating interactions weight for eventIds={}", eventIds, e);
            throw new InteractionCalculationException("Failed to calculate interactions weight", e);
        }
    }
}



