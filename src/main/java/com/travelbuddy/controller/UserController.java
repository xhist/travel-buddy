// src/main/java/com/travelbuddy/controller/UserController.java
package com.travelbuddy.controller;

import com.travelbuddy.dto.ProfileUpdateRequest;
import com.travelbuddy.dto.TripResponse;
import com.travelbuddy.dto.UserDto;
import com.travelbuddy.model.NotificationPreferences;
import com.travelbuddy.model.User;
import com.travelbuddy.security.CustomUserDetails;
import com.travelbuddy.service.interfaces.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    @Autowired
    private IUserService userService;

    @PutMapping("/updateProfile")
    public ResponseEntity<UserDto> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        final var updatedUser = userService.updateUser(request);
        log.info("Profile updated for user {}", updatedUser.getUsername());
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{userId}/trips")
    public ResponseEntity<Set<TripResponse>> getUserTrips(@PathVariable final Long userId) {
        return ResponseEntity.ok(userService.getUserTrips(userId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserProfile(@PathVariable final Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }
}
