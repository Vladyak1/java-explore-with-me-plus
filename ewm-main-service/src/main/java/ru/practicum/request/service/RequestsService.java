package ru.practicum.request.service;


import com.querydsl.core.types.dsl.BooleanExpression;
import jakarta.transaction.Transactional;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;


import java.util.List;

public interface RequestsService {
    List<ParticipationRequestDto> getRequest(Long userId);

    @Transactional
    ParticipationRequestDto setRequest(Long eventId, Long userId);

    @Transactional
    ParticipationRequestDto updateRequest(Long userId, Long requestId);

    List<ParticipationRequest> getAllByEventId(Long eventId);

    List<ParticipationRequest> getAllByRequestIdIn(List<Long> requestIds);

    Iterable<ParticipationRequest> findAll(BooleanExpression conditions);

    List<ParticipationRequest> saveAll(List<ParticipationRequest> requests);

}