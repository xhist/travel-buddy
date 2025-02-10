package com.travelbuddy.controller;

import com.travelbuddy.dto.AuthRequest;
import com.travelbuddy.dto.AuthResponse;
import com.travelbuddy.dto.RegisterRequest;
import com.travelbuddy.model.User;
import com.travelbuddy.security.JwtTokenProvider;
import com.travelbuddy.service.interfaces.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private IUserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(registerRequest.getPassword())
                .build();
        User createdUser = userService.registerUser(user);
        log.info("User {} registered", createdUser.getUsername());
        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest loginRequest) {
        User user = userService.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!userService.getPasswordEncoder().matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Invalid credentials for user {}", loginRequest.getUsername());
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
        String token = tokenProvider.generateToken(user.getUsername());
        log.info("User {} authenticated", user.getUsername());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
