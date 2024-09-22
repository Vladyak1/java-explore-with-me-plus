package ru.practicum.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class EndpointHit {
    Long id;

    @Pattern(regexp = "^ewm-main-service$", message = "Data must come from the main service.")
    private String app;

    @NotNull(message = "URI shouldn't be null.")
    @Pattern(regexp = "^/events(/(?<!-)[1-9][0-9]{0,18})?$",
            message = "URI must match the correct format.")
    private String uri;

    @NotNull(message = "IP shouldn't be null.")
    private String ip;

   /* @NotNull(message = "Parameter 'timestamp' shouldn't be null.")
    @PastOrPresent(message = "Parameter 'timestamp' shouldn't be in future.")
    private LocalDateTime timestamp;

    */
}