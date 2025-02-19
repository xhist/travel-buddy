package com.travelbuddy.service.interfaces;

import com.travelbuddy.dto.ProfileUpdateRequest;
import com.travelbuddy.dto.RegisterRequest;
import com.travelbuddy.dto.TripResponse;
import com.travelbuddy.dto.UserDto;
import com.travelbuddy.model.User;
import java.util.Optional;
import java.util.Set;

public interface IUserService {
    UserDto registerUser(final RegisterRequest registerRequest);
    UserDto updateUser(final ProfileUpdateRequest updateRequest);
    UserDto getUser(final Long userId);
    User findByUsername(final String username);
    // Expose the PasswordEncoder for checking passwords.
    org.springframework.security.crypto.password.PasswordEncoder getPasswordEncoder();
    Set<TripResponse> getUserTrips(final Long userId);
}
