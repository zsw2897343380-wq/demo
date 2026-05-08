package com.example.issue15.auth;

import com.example.issue15.auth.dto.LoginRequest;
import com.example.issue15.auth.dto.RegisterRequest;
import com.example.issue15.auth.dto.AuthResponse;
import com.example.issue15.auth.dto.UserInfoResponse;
import com.example.issue15.auth.exception.AuthenticationException;
import com.example.issue15.auth.exception.RegistrationException;
import com.example.issue15.auth.exception.UserNotFoundException;
import com.example.issue15.auth.model.User;
import com.example.issue15.auth.repository.UserRepository;
import com.example.issue15.auth.security.JwtTokenProvider;
import com.example.issue15.auth.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service class for handling user authentication, registration, and JWT token management.
 * Provides comprehensive user management functionality with proper error handling.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user in the system.
     *
     * @param request the registration request containing username, email, and password
     * @return AuthResponse containing JWT token and user information
     * @throws RegistrationException if registration fails due to duplicate username/email or invalid data
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.debug("Attempting to register user with username: {}", request.getUsername());

        // Validate input
        validateRegistrationRequest(request);

        // Check for existing username
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed - username already exists: {}", request.getUsername());
            throw new RegistrationException("Username is already taken");
        }

        // Check for existing email
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new RegistrationException("Email is already registered");
        }

        // Create new user entity
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Save user to database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(savedUser);

        // Build and return authentication response
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .build();
    }

    /**
     * Authenticates a user and generates JWT token.
     *
     * @param request the login request containing username/email and password
     * @return AuthResponse containing JWT token and user information
     * @throws AuthenticationException if authentication fails
     */
    public AuthResponse login(LoginRequest request) {
        log.debug("Attempting to login user: {}", request.getUsernameOrEmail());

        // Authenticate user
        Authentication authentication = authenticateUser(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user principal
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(userPrincipal);

        log.info("User logged in successfully: {}", userPrincipal.getUsername());

        // Build and return authentication response
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(userPrincipal.getId())
                .username(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .build();
    }

    /**
     * Retrieves user information by user ID.
     *
     * @param userId the ID of the user to retrieve
     * @return UserInfoResponse containing user details
     * @throws UserNotFoundException if user is not found
     */
    public UserInfoResponse getUserInfo(Long userId) {
        log.debug("Fetching user info for user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userId);
                    return new UserNotFoundException("User not found with id: " + userId);
                });

        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Retrieves user information by username.
     *
     * @param username the username to search for
     * @return UserInfoResponse containing user details
     * @throws UserNotFoundException if user is not found
     */
    public UserInfoResponse getUserInfoByUsername(String username) {
        log.debug("Fetching user info for username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found with username: {}", username);
                    return new UserNotFoundException("User not found with username: " + username);
                });

        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Updates user profile information.
     *
     * @param userId      the ID of the user to update
     * @param email       the new email address (optional)
     * @param currentPassword the current password for verification
     * @param newPassword the new password (optional)
     * @return UserInfoResponse containing updated user details
     * @throws UserNotFoundException if user is not found
     * @throws AuthenticationException if current password is incorrect
     */
    @Transactional
    public UserInfoResponse updateUserProfile(Long userId, String email, String currentPassword, String newPassword) {
        log.debug("Updating profile for user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userId);
                    return new UserNotFoundException("User not found with id: " + userId);
                });

        // Verify current password if changing password
        if (newPassword != null && !newPassword.isEmpty()) {
            if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
                log.warn("Password change failed - incorrect current password for user: {}", userId);
                throw new AuthenticationException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        // Update email if provided
        if (email != null && !email.isEmpty() && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                log.warn("Email update failed - email already exists: {}", email);
                throw new RegistrationException("Email is already in use");
            }
            user.setEmail(email);
        }

        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        log.info("User profile updated successfully for user id: {}", userId);

        return UserInfoResponse.builder()
                .id(updatedUser.getId())
                .username(updatedUser.getUsername())
                .email(updatedUser.getEmail())
                .enabled(updatedUser.isEnabled())
                .createdAt(updatedUser.getCreatedAt())
                .updatedAt(updatedUser.getUpdatedAt())
                .build();
    }

    /**
     * Validates a JWT token and returns the associated user information.
     *
     * @param token the JWT token to validate
     * @return UserInfoResponse containing user details
     * @throws AuthenticationException if token is invalid or expired
     */
    public UserInfoResponse validateToken(String token) {
        log.debug("Validating JWT token");

        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("Invalid or expired JWT token");
            throw new AuthenticationException("Invalid or expired token");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        return getUserInfo(userId);
    }

    /**
     * Deletes a user account.
     *
     * @param userId the ID of the user to delete
     * @throws UserNotFoundException if user is not found
     */
    @Transactional
    public void deleteUser(Long userId) {
        log.debug("Deleting user with id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userId);
                    return new UserNotFoundException("User not found with id: "