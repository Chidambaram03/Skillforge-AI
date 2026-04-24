package com.skillforge.app.service;

import com.skillforge.app.model.User;
import com.skillforge.app.model.UserProfile;
import com.skillforge.app.repository.UserProfileRepository;
import com.skillforge.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Transactional
    public User registerUser(String name, String email, String password) {
        // Basic check if user exists
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email already registered!");
        }

        // Create User
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        // Note: In a production app, password MUST be hashed (e.g. BCrypt) here.
        // For simplicity in this demo, storing as plain/simple hash representation.
        user.setPasswordHash(password); 
        user = userRepository.save(user);

        // Create initial default Profile (L1 basic recall)
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setThinkingLevel("L1");
        profile.setAccuracyPercentage(0.0);
        profile.setAvgResponseTimeMs(0.0);
        userProfileRepository.save(profile);

        return user;
    }

    public Optional<User> loginUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Compare plain string for demo (use BCrypt.matches in production)
            if (user.getPasswordHash().equals(password)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}
