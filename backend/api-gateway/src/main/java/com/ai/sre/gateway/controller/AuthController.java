package com.ai.sre.gateway.controller;

import com.ai.sre.common.security.JwtUtil;
import com.ai.sre.gateway.model.UserEntity;
import com.ai.sre.gateway.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * Authentication controller — handles login, registration, and token refresh.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * POST /api/v1/auth/login — Authenticate and return JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        var user = userRepository.findByEmail(request.email())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
        }

        // Update last login
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(),
                Map.of("userId", user.getId().toString(), "fullName", user.getFullName()));

        log.info("User logged in: {} (role: {})", user.getEmail(), user.getRole());

        return ResponseEntity.ok(new AuthResponse(
                token,
                user.getId().toString(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                "Bearer"
        ));
    }

    /**
     * POST /api/v1/auth/register — Register a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email already registered"));
        }

        var user = UserEntity.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(request.role() != null ? request.role() : "ENGINEER")
                .build();

        userRepository.save(user);
        log.info("New user registered: {} (role: {})", user.getEmail(), user.getRole());

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(),
                Map.of("userId", user.getId().toString(), "fullName", user.getFullName()));

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(
                token,
                user.getId().toString(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                "Bearer"
        ));
    }

    /**
     * GET /api/v1/auth/me — Get current user info from JWT.
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        return userRepository.findByEmail(email)
                .map(user -> ResponseEntity.ok(Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "fullName", user.getFullName(),
                        "role", user.getRole()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Request/Response Records ====================

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password,
            @NotBlank String fullName,
            String role
    ) {}

    public record AuthResponse(
            String token,
            String userId,
            String email,
            String fullName,
            String role,
            String tokenType
    ) {}
}
