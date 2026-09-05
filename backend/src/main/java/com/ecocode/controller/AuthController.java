package com.ecocode.controller;

import com.ecocode.config.JwtUtil;
import com.ecocode.dto.AuthRequest;
import com.ecocode.dto.AuthResponse;
import com.ecocode.dto.RegisterRequest;
import com.ecocode.model.User;
import com.ecocode.repository.UserRepository;
import com.ecocode.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            User user = userService.registerUser(req.getUsername(), req.getEmail(), req.getPassword());
            String token = jwtUtil.generateToken(user.getEmail());
            return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        Optional<User> userOpt = userRepository.findByEmail(req.getEmail());
        if (userOpt.isPresent() && passwordEncoder.matches(req.getPassword(), userOpt.get().getPassword())) {
            User user = userOpt.get();
            String token = jwtUtil.generateToken(user.getEmail());
            return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole()));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            // Return default fallback user object for non-authenticated sessions
            return ResponseEntity.ok(Map.of("username", "Developer", "email", "dev@ecocode.io", "role", "DEVELOPER"));
        }
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(Map.of("id", user.getId(), "username", user.getUsername(), "email", user.getEmail(), "role", user.getRole()));
    }
}
