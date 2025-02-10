package com.travelbuddy.service;

import com.travelbuddy.service.interfaces.IWeatherService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WeatherService implements IWeatherService {

    @Value("${app.weather.apiKey}")
    private String apiKey;

    @Override
    public String getWeatherForCity(String city) {
        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey;
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(url, String.class);
        log.info("Weather data fetched for city {}", city);
        return result;
    }
}
