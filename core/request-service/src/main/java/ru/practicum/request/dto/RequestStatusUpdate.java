package ru.practicum.request.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestStatusUpdate {
    private List<Long> requestIds;
    private String status;
}
