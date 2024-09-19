package ru.practicum.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationRequestDto {
    private Integer id; // Идентификатор заявки
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created; // Дата и время создания заявки
    private Integer event; // Идентификатор события
    private Integer requester; // Идентификатор пользователя, отправившего заявку
    private String status; // Статус заявки
}
