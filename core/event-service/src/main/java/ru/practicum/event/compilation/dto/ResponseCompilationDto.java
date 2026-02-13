package ru.practicum.event.compilation.dto;

import lombok.*;
import ru.practicum.internal.event.dto.EventShortDto;
import java.util.Set;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseCompilationDto {
    private Long id;
    private String title;
    private Boolean pinned;
    private Set<EventShortDto> events;
}
