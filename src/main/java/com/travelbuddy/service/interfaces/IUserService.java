package com.travelbuddy.service.interfaces;

import com.travelbuddy.model.User;
import java.util.Optional;

public interface IUserService {
    User registerUser(User user);
    User updateUser(User user);
    Optional<User> findByUsername(String username);
    // Expose the PasswordEncoder for checking passwords.
    org.springframework.security.crypto.password.PasswordEncoder getPasswordEncoder();
}
