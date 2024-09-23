package ru.practicum.event.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.event.model.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(source = "category", target = "category.id")
    @Mapping(target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(source = "location.lat", target = "lat")
    @Mapping(source = "location.lon", target = "lon")
    Event toEvent(NewEventDto newEventDto);

    @Mapping(target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "createdOn", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "publishedOn", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(source = "lat", target = "location.lat")
    @Mapping(source = "lon", target = "location.lon")
    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    EventShortDto toEventShortDto(Event event);

    @Mapping(source = "views", target = "views")
    EventShortDto toShortDto(Event event, Long views);

    @Mapping(source = "event.lat", target = "location.lat")
    @Mapping(source = "event.lon", target = "location.lon")
    @Mapping(source = "views", target = "views")
    EventLongDto toLongDto(Event event, Long views);

    @Mapping(source = "event.lat", target = "location.lat")
    @Mapping(source = "event.lon", target = "location.lon")
    @Mapping(target = "views", constant = "0L")
    EventLongDto toLongDto(Event event);
}
