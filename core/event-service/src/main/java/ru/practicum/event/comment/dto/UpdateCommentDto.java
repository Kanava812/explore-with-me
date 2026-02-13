package ru.practicum.event.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.event.comment.enums.Status;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCommentDto {
    @NotNull(message = "Status can't be null")
    private Status status;
}
