package ru.practicum.service;

import ru.practicum.dto.StatDto;
import ru.practicum.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {
    StatDto postHit(StatDto dto);

    List<ViewStats> getViewStats(LocalDateTime start, LocalDateTime end, String[] uris, Boolean unique);
}