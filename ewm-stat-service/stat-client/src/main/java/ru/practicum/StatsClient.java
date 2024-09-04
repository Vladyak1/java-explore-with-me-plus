package ru.practicum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.stereotype.Service;
import ru.practicum.dto.StatDto;

import java.util.Map;

@Service
public class StatsClient extends BaseClient {

    @Autowired
    public StatsClient(@Value("${ewm-stats-service.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl)).build());
    }

    public ResponseEntity<Object> postHit(StatDto hit) {
        return post(hit);
    }

    public ResponseEntity<Object> getViewStats(Map<String, Object> params) {
        return get(params);
    }
}