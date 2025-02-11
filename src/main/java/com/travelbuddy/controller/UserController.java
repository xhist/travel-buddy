// src/main/java/com/travelbuddy/controller/UserController.java
package com.travelbuddy.controller;

import com.travelbuddy.dto.ProfileUpdateRequest;
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

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private ModelMapper modelMapper;

    @PutMapping("/updateProfile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = currentUser.getUser();
        user.setEmail(request.getEmail());
        user.setProfilePicture(request.getProfilePicture());
        // Update notification preferences
        try {
            LocalTime time = (request.getReminderTime() != null)
                    ? LocalTime.parse(request.getReminderTime()) : null;
            user.setNotificationPreferences(NotificationPreferences.builder()
                    .reminderDaysBefore(request.getReminderDaysBefore())
                    .reminderTime(time)
                    .build());
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid time format. Expected HH:mm");
        }
        // Assume an updateUser method (or re-save the user)
        User updatedUser = userService.updateUser(user);  // For demonstration; ideally use updateUser()
        log.info("Profile updated for user {}", updatedUser.getUsername());
        return ResponseEntity.ok(updatedUser);
    }
}
