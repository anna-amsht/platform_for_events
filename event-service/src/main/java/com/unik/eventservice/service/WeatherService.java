package com.unik.eventservice.service;

import com.unik.eventservice.config.AppProperties;
import com.unik.eventservice.dto.WeatherForecastResponse;
import com.unik.eventservice.entity.Event;
import com.unik.eventservice.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Service
public class WeatherService {
    private final AppProperties properties;
    private final EventRepository eventRepository;
    private final RestClient restClient = RestClient.create("https://api.openweathermap.org");

    public WeatherService(AppProperties properties, EventRepository eventRepository) {
        this.properties = properties;
        this.eventRepository = eventRepository;
    }

    public WeatherForecastResponse forecast(UUID eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        if (!StringUtils.hasText(properties.weather().apiKey()) || event.getLatitude() == null || event.getLongitude() == null) {
            return new WeatherForecastResponse(false, event.getLocation(), "Weather forecast is not configured");
        }
        Map<?, ?> body = restClient.get()
                .uri(uri -> uri.path("/data/2.5/forecast")
                        .queryParam("lat", event.getLatitude())
                        .queryParam("lon", event.getLongitude())
                        .queryParam("appid", properties.weather().apiKey())
                        .queryParam("units", "metric")
                        .build())
                .retrieve()
                .body(Map.class);
        return new WeatherForecastResponse(true, event.getLocation(), String.valueOf(body));
    }
}
