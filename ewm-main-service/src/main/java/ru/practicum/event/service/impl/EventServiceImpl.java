package ru.practicum.event.service.impl;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.category.model.Category;
import ru.practicum.category.service.CategoryService;
import ru.practicum.event.model.enums.EventSort;
import ru.practicum.event.model.enums.EventState;
import ru.practicum.event.model.enums.StateActionForAdmin;
import ru.practicum.event.model.enums.StateActionForUser;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.DataConflictRequest;
import ru.practicum.exception.InvalidRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.mapper.RequestsMapper;
import ru.practicum.request.model.*;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.enums.RequestStatus;
import ru.practicum.request.service.RequestsService;
import ru.practicum.stat.service.StatsService;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@NoArgsConstructor(force = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserService userService;
    private final RequestsService requestService;
    private final CategoryService categoryService;
    private final EventMapper eventMapper;
    private final RequestsMapper requestMapper;
    private final StatsService statsService;

    @Autowired
    @Lazy
    public EventServiceImpl(EventRepository eventRepository, UserService userService, RequestsService requestService,
                            CategoryService categoryService, EventMapper eventMapper, RequestsMapper requestMapper, StatsService statsService) {
        this.eventRepository = eventRepository;
        this.userService = userService;
        this.requestService = requestService;
        this.categoryService = categoryService;
        this.eventMapper = eventMapper;
        this.requestMapper = requestMapper;
        this.statsService = statsService;
    }

    // Часть private

    public List<EventShortDto> getAllEventOfUser(Long userId, Integer from, Integer size) {
        List<EventShortDto> eventsOfUser;
        userService.findUserById(userId);
        List<Event> events = eventRepository.findEventsOfUser(userId, PageRequest.of(from / size, size));
        eventsOfUser = events.stream().map(eventMapper::toEventShortDto).collect(Collectors.toList());
        log.info("Получение всех событий пользователя с ID = {}", userId);
        return eventsOfUser;
    }

    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User initiator = userService.findUserById(userId);
        Category category = categoryService.getCategoryByIdNotMapping(newEventDto.getCategory());
        Event event = eventMapper.toEvent(newEventDto);
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new InvalidRequestException("The date and time for which the event is scheduled cannot be earlier " +
                    "than two hours from the current moment");
        }
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setConfirmedRequests(0L);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setPublishedOn(LocalDateTime.now());
        event = eventRepository.save(event);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        log.info("Событию присвоен ID = {}, и оно успешно добавлено", event.getId());
        return eventFullDto;
    }

    public EventFullDto getEventOfUserById(Long userId, Long eventId) {
        userService.findUserById(userId);
        Optional<Event> optEventSaved = eventRepository.findByIdAndInitiatorId(eventId, userId);
        EventFullDto eventFullDto;
        if (optEventSaved.isPresent()) {
            eventFullDto = eventMapper.toEventFullDto(optEventSaved.get());
        } else {
            throw new NotFoundException("The required object was not found.");
        }
        log.info("Выполнен поиск события с ID = {}", eventId);
        return eventFullDto;
    }

    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateEvent) {
        userService.findUserById(userId);
        Optional<Event> optEventSaved = eventRepository.findByIdAndInitiatorId(eventId, userId);
        Event eventSaved;
        if (optEventSaved.isPresent()) {
            eventSaved = optEventSaved.get();
        } else {
            throw new NotFoundException("Event with ID = " + eventId + " was not found");
        }

        if (eventSaved.getState().equals(EventState.PUBLISHED)) {
            throw new DataConflictRequest("It is not possible to make changes to an already published event.");
        }

        if (updateEvent.getEventDate() != null) {
            if (LocalDateTime.parse(updateEvent.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .isBefore(LocalDateTime.now().plusHours(2))) {
                throw new InvalidRequestException("The start date of the event to be modified must be no earlier " +
                        "than two hours from the date of publication.");
            } else {
                eventSaved.setEventDate(LocalDateTime.parse(updateEvent.getEventDate(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
        }

        if (updateEvent.getStateAction() != null) {
            updateStateOfEventByUser(updateEvent.getStateAction(), eventSaved);
        }

        if (updateEvent.getAnnotation() != null) {
            eventSaved.setAnnotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getCategory() != null) {
            Category category = categoryService.getCategoryByIdNotMapping(updateEvent.getCategory());
            eventSaved.setCategory(category);
        }
        if (updateEvent.getDescription() != null) {
            eventSaved.setDescription(updateEvent.getDescription());
        }
        if (updateEvent.getLocation() != null) {
            eventSaved.setLat(updateEvent.getLocation().getLat());
            eventSaved.setLon(updateEvent.getLocation().getLon());
        }
        if (updateEvent.getParticipantLimit() != null) {
            eventSaved.setParticipantLimit(eventSaved.getParticipantLimit());
        }
        if (updateEvent.getPaid() != null) {
            eventSaved.setPaid(updateEvent.getPaid());
        }
        if (updateEvent.getRequestModeration() != null) {
            eventSaved.setRequestModeration(updateEvent.getRequestModeration());
        }
        if (updateEvent.getTitle() != null) {
            eventSaved.setTitle((updateEvent.getTitle()));
        }

        Event eventUpdate = eventRepository.save(eventSaved);

        EventFullDto eventFullDto = eventMapper.toEventFullDto(eventUpdate);
        log.info("Событие ID = {} пользователя ID = {} успешно обновлено", eventId, userId);
        return eventFullDto;
    }

    public List<ParticipationRequestDto> getRequestEventByUser(Long userId, Long eventId) {
        userService.findUserById(userId);
        getEventById(eventId);
        List<ParticipationRequest> requests = requestService.getAllByEventId(eventId);
        log.info("Получен список заявок на участие в событии с ID = {} пользователя с ID = {}", eventId, userId);
        return requests.stream().map(requestMapper::toParticipationRequestDto).collect(Collectors.toList());
    }

    @Transactional
    public EventRequestStatusUpdateResult changeRequestEventStatus(Long userId, Long eventId,
                                                                   EventRequestStatusUpdateRequest requestUpdate) {
        userService.findUserById(userId);
        Event event = getEventById(eventId);
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new RuntimeException("Пользователь с ID = " + userId + " не является инициатором события с ID = " + eventId);
        }

        List<ParticipationRequest> requests = requestService.getAllByRequestIdIn(requestUpdate.getRequestIds()); // получаем список запросов на одобрение
        RequestStatus newStatus = RequestStatus.valueOf(requestUpdate.getStatus()); // получаем значение нового статуса
        Integer countOfRequest = requestUpdate.getRequestIds().size(); // находим количество новых заявок на одобрение

        for (ParticipationRequest request : requests) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new DataConflictRequest("Изменить статус можно только у ожидающей подтверждения заявки на " +
                        "участие");
            }
        }

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        switch (newStatus) {
            case CONFIRMED:
                if ((event.getParticipantLimit() == 0) ||
                        ((event.getConfirmedRequests() + countOfRequest.longValue()) < event.getParticipantLimit()) ||
                        (!event.getRequestModeration())) {
                    requests.forEach(request -> request.setStatus(RequestStatus.CONFIRMED));
                    event.setConfirmedRequests(event.getConfirmedRequests() + countOfRequest);
                    for (ParticipationRequest request : requests) {
                        confirmedRequests.add(requestMapper.toParticipationRequestDto(request));
                    }
                } else if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
                    throw new DataConflictRequest("The limit on applications for this event has been reached");
                } else {
                    for (ParticipationRequest request : requests) {
                        if (event.getConfirmedRequests() < event.getParticipantLimit()) {
                            request.setStatus(RequestStatus.CONFIRMED);
                            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                            confirmedRequests.add(requestMapper.toParticipationRequestDto(request));
                        } else {
                            request.setStatus(RequestStatus.REJECTED);
                            rejectedRequests.add(requestMapper.toParticipationRequestDto(request));
                        }
                    }
                }
                break;
            case REJECTED:
                for (ParticipationRequest request : requests) {
                    request.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(requestMapper.toParticipationRequestDto(request));
                }
                break;
        }
        eventRepository.save(event);
        requestService.saveAll(requests);
        log.info("Статусы заявок успешно обновлены");
        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }


    // Часть admin

    public List<EventFullDto> getAllEventsByAdmin(EventAdminParams eventAdminParams) {

        //формируем условие выборки
       /* BooleanExpression conditions = makeEventsQueryConditionsForAdmin(eventAdminParams);

        //настройка размера страницы
        PageRequest pageRequest = PageRequest.of(
                eventAdminParams.getFrom() / eventAdminParams.getSize(), eventAdminParams.getSize());

        //запрашиваем события из базы
        List<Event> events = eventRepository.findAll(conditions, pageRequest).toList();

        //Запрашиваем количество просмотров каждого события

        List<EventFullDto> eventsFullDto = events.stream().map(eventMapper::toEventFullDto).collect(Collectors.toList());

        log.info("События успешно выгружены");
        return eventsFullDto;

        */
        return null;
    }

    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEvent) {
        Optional<Event> optEventSaved = eventRepository.findById(eventId);
        Event eventSaved;
        if (optEventSaved.isPresent()) {
            eventSaved = optEventSaved.get();
        } else {
            throw new NotFoundException("Event with ID = " + eventId + " was not found");
        }
        if (updateEvent.getEventDate() != null) {
            updateEventData(updateEvent.getEventDate(), eventSaved);
        }
        if (updateEvent.getStateAction() != null) {
            updateStateOfEventByAdmin(updateEvent.getStateAction(), eventSaved);
        }
        if (updateEvent.getAnnotation() != null) {
            eventSaved.setAnnotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getCategory() != null) {
            Category category = categoryService.getCategoryByIdNotMapping(updateEvent.getCategory());
            eventSaved.setCategory(category);
        }
        if (updateEvent.getDescription() != null) {
            eventSaved.setDescription(updateEvent.getDescription());
        }
        if (updateEvent.getLocation() != null) {
            eventSaved.setLat(updateEvent.getLocation().getLat());
            eventSaved.setLon(updateEvent.getLocation().getLon());
        }
        if (updateEvent.getParticipantLimit() != null) {
            eventSaved.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getPaid() != null) {
            eventSaved.setPaid(updateEvent.getPaid());
        }
        if (updateEvent.getRequestModeration() != null) {
            eventSaved.setRequestModeration(updateEvent.getRequestModeration());
        }
        if (updateEvent.getTitle() != null) {
            eventSaved.setTitle((updateEvent.getTitle()));
        }

        eventSaved = eventRepository.save(eventSaved);

        EventFullDto eventFullDto = eventMapper.toEventFullDto(eventSaved);

        log.info("Событие ID = {} успешно обновлено от имени администратора", eventId);
        return eventFullDto;
    }

    @Override
    public List<EventShortDto> getPublicEvents(EventPublicParams param) {
        log.info("Запрос получить опубликованные события");

        if (param.getRangeStart().isAfter(param.getRangeEnd())) {
            log.error("NotValid. При поиске опубликованных событий rangeStart после rangeEnd.");
            throw new InvalidRequestException("The start of the range must be before the end of the range.");
        }

        List<Event> events = eventRepository.searchPublicEvents(param);

        Comparator<EventShortDto> comparator = Comparator.comparing(EventShortDto::getId);

        if ((param.getSort() != null) && (param.getSort().equals(EventSort.EVENT_DATE))) {
            comparator = Comparator.comparing(EventShortDto::getEventDate);
        } else if ((param.getSort() != null) && (param.getSort().equals(EventSort.VIEWS))) {
            comparator = Comparator.comparing(EventShortDto::getViews, Comparator.reverseOrder());
        }

        Map<Long, Long> view = getView(events, false);
        return events.stream()
                .map(e -> {
                    assert eventMapper != null;
                    return eventMapper.toShortDto(e, view.getOrDefault(e.getId(), 0L));
                })
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private Map<Long, Long> getView(List<Event> events, boolean unique) {
        if (!events.isEmpty()) {
            List<Long> eventsId = events.stream()
                    .map(Event::getId)
                    .collect(Collectors.toList());
            return statsService.getView(eventsId, unique);
        } else return new HashMap<>();
    }

    // Часть public
    public List<EventShortDto> getAllEventsByUser(EventPublicParams request, HttpServletRequest httpServletRequest) {

       /* if (request.getRangeStart() != null && request.getRangeEnd() != null
                && request.getRangeStart().isAfter(request.getRangeEnd())) {
            throw new InvalidRequestException("The start date of the event to be modified must be no earlier " +
                    "than one hour from the date of publication.");
        }

        // Создаем условия выборки
        Predicate conditions = makeEventsQueryConditionsForPublic(request);

        // Пагинация и сортировка
        PageRequest pageRequest = PageRequest.of(request.getFrom() / request.getSize(), request.getSize());

        // Выполняем запрос в репозиторий с условиями и пагинацией
        Page<Event> eventsPage = eventRepository.findAll(conditions, pageRequest);
        List<Event> events = eventsPage.getContent();

        // Обработка результатов
        Map<Long, Long> eventToRequestsCount = getEventRequests(events);

        List<EventShortDto> eventsShortDto = events.stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());

        for (EventShortDto eventShortDto : eventsShortDto) {
            eventShortDto.setConfirmedRequests(eventToRequestsCount.get(eventShortDto.getId()));
        }

        log.info("События успешно выгружены");
        return eventsShortDto.stream().sorted(new EventSortByEventDate()).collect(Collectors.toList());

        */
        return null;
    }



    public EventFullDto getEventDtoById(Long id, HttpServletRequest httpServletRequest) {

        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event must be published"));

        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);

        log.info("Событие ID = {} успешно обновлено от имени администратора", id);
        return eventFullDto;
    }


    // ----- Вспомогательная часть ----

    // Вспомогательная функция обновления статуса
    private void updateStateOfEventByUser(String stateAction, Event eventSaved) {
        StateActionForUser stateActionForUser;
        try {
            stateActionForUser = StateActionForUser.valueOf(stateAction);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid parameter stateAction");
        }
        switch (stateActionForUser) {
            case SEND_TO_REVIEW:
                eventSaved.setState(EventState.PENDING);
                break;
            case CANCEL_REVIEW:
                eventSaved.setState(EventState.CANCELED);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + stateAction);
        }
    }

    // Вспомогательная функция обновления статуса и время публикации
    private void updateStateOfEventByAdmin(String stateAction, Event eventSaved) {
        StateActionForAdmin stateActionForAdmin;
        try {
            stateActionForAdmin = StateActionForAdmin.valueOf(stateAction);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid parameter stateAction");
        }
        switch (stateActionForAdmin) {
            case REJECT_EVENT:
                if (eventSaved.getState().equals(EventState.PUBLISHED)) {
                    throw new DataConflictRequest("The event has already been published.");
                }
                eventSaved.setState(EventState.CANCELED);
                break;
            case PUBLISH_EVENT:
                if (!eventSaved.getState().equals(EventState.PENDING)) {
                    throw new DataConflictRequest("Cannot publish the event because it's not in the right state: PUBLISHED");
                }
                eventSaved.setState(EventState.PUBLISHED);
                eventSaved.setPublishedOn(LocalDateTime.now());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + stateAction);
        }
    }

    // Получение event по id

    public Event getEventById(Long eventId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isPresent()) {
            return eventOptional.get();
        }
        throw new NotFoundException("Event with ID = " + eventId + " was not found");
    }


    public void addRequestToEvent(Event event) {
        eventRepository.save(event);
    }

    // Собираем условие по которому будем выбирать события из базы данных для публичного запроса
    private Predicate makeEventsQueryConditionsForPublic(EventPublicParams request) {
        QEvent event = QEvent.event;
        BooleanExpression condition = event.isNotNull(); // Базовое условие

        if (request.getText() != null && !request.getText().isEmpty()) {
            String searchText = "%" + request.getText().toLowerCase() + "%";
            condition = condition.and(
                    event.title.likeIgnoreCase(searchText)
                            .or(event.annotation.likeIgnoreCase(searchText))
                            .or(event.description.likeIgnoreCase(searchText))
            );
        }

        // Добавление других условий фильтрации (категории, платность и т.д.)
        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            condition = condition.and(event.category.id.in(request.getCategories()));
        }

        if (request.getPaid() != null) {
            condition = condition.and(event.paid.eq(request.getPaid()));
        }

        if (request.getRangeStart() != null) {
            condition = condition.and(event.eventDate.goe(request.getRangeStart()));
        }

        if (request.getRangeEnd() != null) {
            condition = condition.and(event.eventDate.loe(request.getRangeEnd()));
        }

        return condition;
    }


    // Собираем количество одобренных заявок для каждого события
    private Map<Long, Long> getEventRequests(List<Event> events) {
        QParticipationRequest request = QParticipationRequest.participationRequest;

        BooleanExpression condition = request.status.eq(RequestStatus.CONFIRMED).and(request.event.in(events));

        Iterable<ParticipationRequest> reqs = requestService.findAll(condition);
        return StreamSupport
                .stream(reqs.spliterator(), false)
                .collect(Collectors.groupingBy(r -> r.getEvent().getId(), Collectors.counting()));
    }

    // Компаратор для сортировки по дате события
    public static class EventSortByEventDate implements Comparator<EventShortDto> {

        @Override
        public int compare(EventShortDto o1, EventShortDto o2) {
            return o1.getEventDate().compareTo(o2.getEventDate());
        }

    }

    // Собираем условие по которому будем выбирать события из базы данных для запроса администратора
    private static BooleanExpression makeEventsQueryConditionsForAdmin(EventAdminParams request) {
        QEvent event = QEvent.event;

        List<BooleanExpression> conditions = new ArrayList<>();

        // фильтрация по списку пользователь
        if (request.getUsers() != null && !request.getUsers().isEmpty()) {
            conditions.add(
                    event.initiator.id.in(request.getUsers())
            );
        }

        //    фильтрация событий по статусу
        if (request.getStates() != null && !request.getStates().isEmpty()) {
            List<EventState> states = request.getStates().stream().map(EventState::valueOf).collect(Collectors.toList());
            conditions.add(
                    QEvent.event.state.in(states)
            );
        }

        // фильтрация по списку категорий
        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            conditions.add(
                    event.category.id.in(request.getCategories())
            );
        }

//         фильтрация по временному диапазону, если не указано начало, то выборку производим начиная с настоящего
//         времени только в будущее
        LocalDateTime rangeStart;
        if (request.getRangeStart() != null) {
            rangeStart = request.getRangeStart();
        } else {
            rangeStart = LocalDateTime.now();
        }
        conditions.add(
                event.eventDate.goe(rangeStart)
        );

        if (request.getRangeEnd() != null) {
            conditions.add(
                    event.eventDate.loe(request.getRangeEnd())
            );
        }

        return conditions
                .stream()
                .reduce(BooleanExpression::and)
                .get();
    }

    public List<Event> getAllEventsByListId(List<Long> eventsId) {
        return eventRepository.findAllById(eventsId);
    }

    public void updateEventData(String dataTime, Event eventSaved) {
        if (LocalDateTime.parse(dataTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .isBefore(LocalDateTime.now().plusHours(1))) {
            throw new InvalidRequestException("The start date of the event to be modified must be no earlier " +
                    "than one hour from the date of publication.");
        }
        eventSaved.setEventDate(LocalDateTime.parse(dataTime,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    public Optional<Event> findByCategory(Category category) {
        return eventRepository.findByCategory(category);
    }
}