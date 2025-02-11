package com.travelbuddy.controller;

import com.travelbuddy.dto.AuthRequest;
import com.travelbuddy.dto.AuthResponse;
import com.travelbuddy.dto.RegisterRequest;
import com.travelbuddy.dto.UserDto;
import com.travelbuddy.model.User;
import com.travelbuddy.security.JwtTokenProvider;
import com.travelbuddy.service.interfaces.IUserService;
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
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private IUserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ModelMapper modelMapper;

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
        try {
            User user = userService.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + loginRequest.getUsername()));

            if (!userService.getPasswordEncoder().matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "Invalid credentials"));
            }

            String token = tokenProvider.generateToken(user.getUsername());
            UserDto userDto = modelMapper.map(user, UserDto.class);

            // Add additional logging
            log.info("User {} successfully authenticated", user.getUsername());

            return ResponseEntity.ok(new AuthResponse(token, userDto));

        } catch (UsernameNotFoundException e) {
            log.warn("Login attempt failed for username: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Invalid credentials"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

            return ResponseEntity.ok(modelMapper.map(user, UserDto.class));
        } catch (Exception e) {
            log.error("Error getting current user: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
