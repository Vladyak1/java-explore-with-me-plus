package ru.practicum.request.service;

import jakarta.transaction.Transactional;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestsService {
    List<ParticipationRequestDto> getRequest(Long userId);

    @Transactional
    ParticipationRequestDto setRequest(Long eventId, Long userId);

    @Transactional
    ParticipationRequestDto updateRequest(Long userId, Long requestId);
}