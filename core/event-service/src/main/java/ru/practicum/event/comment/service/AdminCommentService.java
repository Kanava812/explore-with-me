package ru.practicum.event.comment.service;

import ru.practicum.event.comment.dto.ResponseCommentDto;
import ru.practicum.event.comment.dto.UpdateCommentDto;
import ru.practicum.event.comment.enums.Status;

import java.util.List;

public interface AdminCommentService {

    List<ResponseCommentDto> getAll(Status status, int from, int size);

    List<ResponseCommentDto> getByEventId(long eventId, Status status);

    void update(long eventId, long commentId, UpdateCommentDto commentDto);

}
