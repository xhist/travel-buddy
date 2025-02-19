package com.travelbuddy.service;

import com.travelbuddy.dto.ProfileUpdateRequest;
import com.travelbuddy.dto.RegisterRequest;
import com.travelbuddy.dto.TripResponse;
import com.travelbuddy.dto.UserDto;
import com.travelbuddy.exception.ResourceNotFoundException;
import com.travelbuddy.model.User;
import com.travelbuddy.model.Role;
import com.travelbuddy.repository.UserRepository;
import com.travelbuddy.service.interfaces.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService implements IUserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDto registerUser(final RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            log.error("Username {} already taken!", request.getUsername());
            throw new RuntimeException("Username already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("Email {} already in use!", request.getEmail());
            throw new RuntimeException("Email already in use!");
        }
        final var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(Role.ROLE_USER))
                .profilePicture(request.getProfilePicture())
                .bio(request.getBio())
                .build();
        final var savedUser = userRepository.save(user);
        log.info("User {} registered successfully.", savedUser.getUsername());
        return UserDto.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .profilePicture(savedUser.getProfilePicture())
                .build();
    }

    @Override
    public UserDto updateUser(final ProfileUpdateRequest request) {
        final var user = userRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User %d does not exist"
                        .formatted(request.getId())));
        user.setProfilePicture(request.getProfilePicture());
        user.setEmail(request.getEmail());
        user.setBio(request.getBio());
        final var updatedUser = userRepository.save(user);
        log.info("User {} updated successfully.", updatedUser.getUsername());
        return UserDto.builder()
                .id(updatedUser.getId())
                .username(updatedUser.getUsername())
                .email(updatedUser.getEmail())
                .profilePicture(updatedUser.getProfilePicture())
                .build();
    }

    @Override
    public UserDto getUser(Long userId) {
        final var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User %d does not exist"
                        .formatted(userId)));
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .build();
    }

    @Override
    public User findByUsername(final String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User %s not found"
                        .formatted(username)));
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    @Override
    public Set<TripResponse> getUserTrips(final Long userId) {
        final var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User %d not found"
                        .formatted(userId)));
        return user.getTrips().stream()
                .map(trip -> TripResponse.builder()
                        .id(trip.getId())
                        .title(trip.getTitle())
                        .startDate(trip.getStartDate())
                        .endDate(trip.getEndDate())
                        .destination(trip.getDestination())
                        .build())
                .collect(Collectors.toSet());
    }
}
