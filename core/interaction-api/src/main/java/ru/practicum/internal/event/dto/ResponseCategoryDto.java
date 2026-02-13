package ru.practicum.internal.event.dto;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseCategoryDto {
    private Long id;
    private String name;
}
