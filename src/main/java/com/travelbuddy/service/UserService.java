package com.travelbuddy.service;

import com.travelbuddy.model.User;
import com.travelbuddy.model.Role;
import com.travelbuddy.repository.UserRepository;
import com.travelbuddy.service.interfaces.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class UserService implements IUserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            log.warn("Username {} already taken!", user.getUsername());
            throw new RuntimeException("Username already taken!");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("Email {} already in use!", user.getEmail());
            throw new RuntimeException("Email already in use!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of(Role.ROLE_USER));
        User savedUser = userRepository.save(user);
        log.info("User {} registered successfully.", savedUser.getUsername());
        return savedUser;
    }

    @Override
    public User updateUser(User user) {
        if (!userRepository.existsByUsername(user.getUsername())) {
            log.warn("User [Username:{}] does not exist!", user.getUsername());
            throw new RuntimeException("User does not exist!");
        }
        final var updatedUser = userRepository.save(user);
        log.info("User {} updated successfully.", updatedUser.getUsername());
        return updatedUser;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}
