package ru.practicum.stat.service.impl;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.Formatter;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.stat.client.StatsClient;
import ru.practicum.stat.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class StatsServiceImpl implements StatsService {
    private static final String APP_NAME = "ewm-service";
    private final DateTimeFormatter formatter = Formatter.getFormatter();
    private final StatsClient statsClient;


    @Override
    public void createStats(String uri, String ip) {
        log.info("Отправка информации в сервис статистики для uri {}", uri);

        EndpointHit stats = EndpointHit.builder()
                .app(APP_NAME)
                .uri(uri)
                .ip(ip)
                //.timestamp(LocalDateTime.now())
                .build();
        //ViewStats receivedDto = ;
        log.info("Информация сохранена {}", statsClient.createStats(stats));
    }

    @Override
    public List<ViewStats> getStats(List<Long> eventsId, boolean unique) {
        log.info("Получение статистики с сервиса статистики для events {}", eventsId);

        String start = LocalDateTime.now().minusYears(20).format(formatter);
        String end = LocalDateTime.now().plusYears(20).format(formatter);

        String[] uris = eventsId.stream()
                .map(id -> String.format("/events/%d", id))
                .toArray(String[]::new);

        return statsClient.getStats(start, end, uris, unique);
    }

    @Override
    public Map<Long, Long> getView(List<Long> eventsId, boolean unique) {
        log.info("Получение просмотров с сервиса статистики для events {}", eventsId);

        List<ViewStats> stats = getStats(eventsId, unique);
        Map<Long, Long> views = new HashMap<>();
        for (ViewStats stat : stats) {
            Long id = Long.valueOf(stat.getUri().replace("/events/", ""));
            Long view = stat.getHits();
            views.put(id, view);
        }
        return views;
    }
}