package ru.practicum.model;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.practicum.dto.EndpointHit;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StatsMapper {
    Hit toModel(EndpointHit createStatsDto);

    EndpointHit toCreationDto(Hit statsModel);
}