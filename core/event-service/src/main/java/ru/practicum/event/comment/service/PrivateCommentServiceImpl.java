package ru.practicum.event.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.internal.error.exception.NotFoundException;
import ru.practicum.internal.error.exception.RuleViolationException;
import ru.practicum.internal.event.enums.EventState;
import ru.practicum.internal.user.dto.UserDto;

import ru.practicum.internal.user.client.UserInternalClient;
import ru.practicum.event.comment.dao.CommentRepository;
import ru.practicum.event.comment.dto.NewCommentDto;
import ru.practicum.event.comment.dto.ResponseCommentDto;
import ru.practicum.event.comment.enums.Status;
import ru.practicum.event.comment.mapper.CommentMapper;
import ru.practicum.event.comment.model.Comment;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.model.Event;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrivateCommentServiceImpl implements PrivateCommentService {
    private final UserInternalClient userInternalClient;
    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    public ResponseCommentDto create(Long userId, Long eventId, NewCommentDto dto) {
        UserDto userDto = userInternalClient.getUserById(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            log.error("Ожидается статус события - PUBLISHED, текущий статус: {}", event.getState());
            throw new RuleViolationException("Неопубликованное событие нельзя комментировать.");
        }

        Comment comment = commentMapper.toComment(dto, event, userDto);
        commentRepository.save(comment);

        log.info("Новый комментарий создан: {}", comment);
        return commentMapper.toResponseCommentDto(comment);
    }

    public ResponseCommentDto patch(Long userId, Long eventId, Long commentId, NewCommentDto dto) {
        Comment comment = validateComment(userId, eventId, commentId);
        commentMapper.updateCommentTextFromDto(dto, comment);
        log.info("Комментарий с ID {} изменен.", commentId);
        return commentMapper.toResponseCommentDto(comment);
    }

    public void delete(Long userId, Long eventId, Long commentId) {
        validateComment(userId, eventId, commentId);
        commentRepository.deleteById(commentId);
        log.info("Комментарий с ID {} успешно удален.", commentId);
    }

    private Comment validateComment(Long userId, Long eventId, Long commentId) {
        userInternalClient.getUserById(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с ID " + commentId + " не найден."));

        if (!comment.getAuthorId().equals(userId)) {
            log.error("Пользователь с ID {} не является автором комментария с ID {}", userId, commentId);
            throw new RuleViolationException("Комментарий можен быть изменен/удален только его автором.");
        }

        if (!comment.getEvent().equals(event)) {
            log.error("Комментарий с ID {} не относится к событию с ID {}", commentId, eventId);
            throw new RuleViolationException("Комментарий должен соответствовать указанному событию.");
        }

        if (!comment.getStatus().equals(Status.PENDING)) {
            log.error("Ожидается статус коммента - PENDING, текущий статус: {}", comment.getStatus());
            throw new RuleViolationException("Комментарий не доступен для редактирования/удаления после публикации или " +
                    "отклонения администратором.");
        }

        return comment;
    }
}
