package ru.practicum.stat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.stat.client.StatsClient;
import ru.practicum.stat.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private static final String APP_NAME = "ewm-service";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    private final StatsClient statsClient;

    @Override
    public void createStats(String uri, String ip) {
        log.info("Creating stats for URI: {}, IP: {}", uri, ip);

        EndpointHit stats = EndpointHit.builder()
                .app(APP_NAME)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now().format(formatter))
                .build();

        try {
            Object result = statsClient.createStats(stats);
            log.info("Stats created successfully. Result: {}", result);
        } catch (Exception e) {
            log.error("Error creating stats: ", e);
            // Consider rethrowing the exception or handling it as appropriate for your application
        }
    }

    @Override
    public List<ViewStats> getStats(List<Long> eventsId, boolean unique) {
        log.info("Getting stats for events: {}, unique: {}", eventsId, unique);

        String start = LocalDateTime.now().minusYears(20).format(formatter);
        String end = LocalDateTime.now().plusYears(20).format(formatter);

        String[] uris = eventsId.stream()
                .map(id -> String.format("/events/%d", id))
                .toArray(String[]::new);

        try {
            List<ViewStats> stats = statsClient.getStats(start, end, uris, unique);
            log.info("Retrieved {} stats entries", stats.size());
            return stats;
        } catch (Exception e) {
            log.error("Error getting stats: ", e);
            // Consider rethrowing the exception or handling it as appropriate for your application
            return Collections.emptyList();
        }
    }

    @Override
    public Map<Long, Long> getView(List<Long> eventsId, boolean unique) {
        log.info("Getting views for events: {}, unique: {}", eventsId, unique);

        Map<Long, Long> views = new HashMap<>();

        // Retrieve stats using the getStats method
        List<ViewStats> stats = getStats(eventsId, unique);

        for (ViewStats stat : stats) {
            String uriPath = stat.getUri();
            if (uriPath.startsWith("/events/")) {
                try {
                    Long id = Long.valueOf(uriPath.substring("/events/".length()));
                    Long hits = stat.getHits();
                    views.put(id, hits);
                } catch (NumberFormatException e) {
                    log.warn("Invalid event ID in URI: {}", uriPath);
                }
            }
        }

        log.info("Processed views: {}", views);
        return views;
    }
}
