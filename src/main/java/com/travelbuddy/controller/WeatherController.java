package com.travelbuddy.controller;

import com.travelbuddy.dto.AuthRequest;
import com.travelbuddy.dto.AuthResponse;
import com.travelbuddy.dto.RegisterRequest;
import com.travelbuddy.dto.UserDto;
import com.travelbuddy.model.User;
import com.travelbuddy.security.JwtTokenProvider;
import com.travelbuddy.service.interfaces.IUserService;
import com.travelbuddy.service.interfaces.IWeatherService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/api/weather")
@Slf4j
public class WeatherController {

    @Autowired
    private IWeatherService weatherService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{city}")
    public ResponseEntity<String> getWeather(@PathVariable String city) {
        return ResponseEntity.ok(weatherService.getWeatherForCity(city));
    }
}
