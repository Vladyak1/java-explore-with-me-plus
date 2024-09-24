package ru.practicum.mappper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.EndpointHit;
import ru.practicum.model.Hit;


@Mapper(componentModel = org.mapstruct.MappingConstants.ComponentModel.SPRING)
public interface HitMapper {
    @Mapping(target = "id", ignore = true)
    Hit toHit(EndpointHit endpointHit);
}