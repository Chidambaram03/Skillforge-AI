package com.skillforge.app.controller;

import com.skillforge.app.dto.AuthRequestDto;
import com.skillforge.app.model.User;
import com.skillforge.app.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Allows calls from Vanilla HTML/JS port
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody AuthRequestDto request) {
        try {
            User user = authService.registerUser(request.getName(), request.getEmail(), request.getPassword());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", user.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody AuthRequestDto request) {
        Optional<User> userOpt = authService.loginUser(request.getEmail(), request.getPassword());
        if (userOpt.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("userId", userOpt.get().getId());
            response.put("userName", userOpt.get().getName());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}
