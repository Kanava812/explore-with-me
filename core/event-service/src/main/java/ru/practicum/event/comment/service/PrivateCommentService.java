package ru.practicum.event.comment.service;

import ru.practicum.event.comment.dto.NewCommentDto;
import ru.practicum.event.comment.dto.ResponseCommentDto;

public interface PrivateCommentService {
    ResponseCommentDto create(Long userId, Long eventId, NewCommentDto dto);

    ResponseCommentDto patch(Long userId, Long eventId, Long commentId, NewCommentDto dto);

    void delete(Long userId, Long eventId, Long commentId);
}
