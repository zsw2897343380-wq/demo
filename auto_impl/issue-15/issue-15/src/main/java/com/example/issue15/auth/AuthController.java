package com.example.issue15.auth;

import com.example.issue15.auth.dto.LoginRequest;
import com.example.issue15.auth.dto.RegisterRequest;
import com.example.issue15.auth.dto.AuthResponse;
import com.example.issue15.auth.dto.UserInfoResponse;
import com.example.issue15.auth.exception.AuthenticationException;
import com.example.issue15.auth.exception.RegistrationException;
import com.example.issue15.auth.exception.UserNotFoundException;
import com.example.issue15.auth.model.User;
import com.example.issue15.auth.service.AuthService;
import com.example.issue15.auth.service.JwtService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for handling authentication and user management operations.
 * Provides endpoints for user registration, login, token validation, and user information retrieval.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtService jwtService;

    /**
     * Constructor-based dependency injection for AuthService and JwtService.
     *
     * @param authService the authentication service
     * @param jwtService  the JWT token service
     */
    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new user in the system.
     *
     * @param registerRequest the registration request containing username, email, and password
     * @return ResponseEntity containing the authentication response with JWT token and user info
     * @throws RegistrationException if registration fails due to duplicate username/email or invalid data
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        logger.info("Processing registration request for username: {}", registerRequest.getUsername());

        try {
            // Register the user and generate authentication tokens
            AuthResponse authResponse = authService.register(registerRequest);
            logger.info("User registered successfully: {}", registerRequest.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
        } catch (RegistrationException e) {
            logger.error("Registration failed for username {}: {}", registerRequest.getUsername(), e.getMessage());
            throw e;
        }
    }

    /**
     * Authenticates a user and generates JWT tokens.
     *
     * @param loginRequest the login request containing username and password
     * @return ResponseEntity containing the authentication response with JWT token and user info
     * @throws AuthenticationException if authentication fails due to invalid credentials
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Processing login request for username: {}", loginRequest.getUsername());

        try {
            // Authenticate the user and generate tokens
            AuthResponse authResponse = authService.login(loginRequest);
            logger.info("User logged in successfully: {}", loginRequest.getUsername());
            return ResponseEntity.ok(authResponse);
        } catch (AuthenticationException e) {
            logger.error("Login failed for username {}: {}", loginRequest.getUsername(), e.getMessage());
            throw e;
        }
    }

    /**
     * Validates the provided JWT token and returns its status.
     *
     * @param token the JWT token to validate
     * @return ResponseEntity containing token validation status and user information if valid
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestParam("token") String token) {
        logger.debug("Validating JWT token");

        Map<String, Object> response = new HashMap<>();

        if (jwtService.validateToken(token)) {
            String username = jwtService.extractUsername(token);
            response.put("valid", true);
            response.put("username", username);
            response.put("message", "Token is valid");
            logger.debug("Token validation successful for user: {}", username);
            return ResponseEntity.ok(response);
        } else {
            response.put("valid", false);
            response.put("message", "Token is invalid or expired");
            logger.debug("Token validation failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Retrieves the current authenticated user's information.
     *
     * @return ResponseEntity containing the user's profile information
     * @throws UserNotFoundException if the authenticated user is not found
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser() {
        // Get the authenticated user from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        logger.info("Fetching user info for: {}", username);

        try {
            User user = authService.findByUsername(username);
            UserInfoResponse userInfo = UserInfoResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();

            return ResponseEntity.ok(userInfo);
        } catch (UserNotFoundException e) {
            logger.error("User not found: {}", username);
            throw e;
        }
    }

    /**
     * Refreshes the JWT token for the authenticated user.
     *
     * @return ResponseEntity containing the new authentication response with refreshed token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        logger.info("Refreshing token for user: {}", username);

        try {
            AuthResponse authResponse = authService.refreshToken(username);
            logger.info("Token refreshed successfully for user: {}", username);
            return ResponseEntity.ok(authResponse);
        } catch (AuthenticationException e) {
            logger.error("Token refresh failed for user {}: {}", username, e.getMessage());
            throw e;
        }
    }

    /**
     * Logs out the current user by invalidating their session.
     *
     * @return ResponseEntity with logout confirmation message
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        logger.info("Processing logout request for user: {}", username);

        // Perform logout operations (invalidate token, clear session, etc.)
        authService.logout(username);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");
        response.put("username", username);

        logger.info("User logged out successfully: {}", username);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the current user's profile information.
     *
     * @param updateRequest the request containing fields to update
     * @return ResponseEntity containing the updated user information
     */
    @PutMapping("/profile")
    public ResponseEntity<UserInfoResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest updateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        logger.info("Updating profile for user: {}", username);

        try {
            User updatedUser = authService.updateProfile(username, updateRequest);
            UserInfoResponse userInfo = UserInfoResponse.builder()
                    .id(updatedUser.getId())
                    .username(updatedUser.getUsername())
                    .email(updatedUser.getEmail())
                    .createdAt(updatedUser.getCreatedAt())
                    .updatedAt(updatedUser.getUpdatedAt())
                    .build();

            logger.info("Profile updated successfully for user: {}", username);
            return ResponseEntity.ok(userInfo);
        } catch (UserNotFoundException e) {
            logger.error("User not found during profile update: {}", username);
            throw e;
        }
    }

    /**
     * Exception handler for AuthenticationException.
     *
     * @param e the authentication exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(AuthenticationException e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Authentication failed");
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Exception handler for RegistrationException.
     *
     * @param e the registration exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<Map<String, String>> handleRegistrationException(RegistrationException e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Registration failed");
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Exception handler for UserNotFoundException.
     *
     * @param e the user not found exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException e) {
        Map<String, String> errorResponse = new