package ru.practicum.event.service;

import ru.practicum.category.model.Category;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

public interface EventService {

    // Часть private
    List<EventShortDto> getAllEventOfUser(Long userId, Integer from, Integer size);

    EventLongDto createEvent(Long userId, NewEventDto newEventDto);

    EventLongDto getEventOfUserById(Long userId, Long eventId);

    EventLongDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<ParticipationRequestDto> getRequestEventByUser(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeRequestEventStatus(Long userId, Long eventId,
                                                            EventRequestStatusUpdateRequest request);

    // Часть admin
    List<EventLongDto> getAllEventsByAdmin(EventAdminParams eventAdminParams);

    EventLongDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);


    List<EventShortDto> getPublicEvents(EventPublicParams param);

    // Часть public
    List<EventShortDto> getAllEventsByUser(EventPublicParams eventPublicParams, HttpServletRequest httpServletRequest);

    EventLongDto getEventDtoById(Long id, HttpServletRequest httpServletRequest);


    // Вспомогательная часть
    Event getEventById(Long eventId);

    void addRequestToEvent(Event event);

    List<Event> getAllEventsByListId(List<Long> eventsId);

    Optional<Event> findByCategory(Category category);

}
