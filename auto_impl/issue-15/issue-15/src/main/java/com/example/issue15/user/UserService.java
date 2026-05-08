package com.example.issue15.user;

import com.example.issue15.user.dto.LoginRequest;
import com.example.issue15.user.dto.RegisterRequest;
import com.example.issue15.user.dto.UserResponse;
import com.example.issue15.user.exception.UserAlreadyExistsException;
import com.example.issue15.user.exception.UserNotFoundException;
import com.example.issue15.user.exception.InvalidCredentialsException;
import com.example.issue15.user.model.User;
import com.example.issue15.user.repository.UserRepository;
import com.example.issue15.user.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Service class for managing user operations including registration, authentication,
 * and profile management.
 * 
 * This service provides the business logic for user management operations
 * and interacts with the UserRepository for data persistence.
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Constructor-based dependency injection for required components.
     *
     * @param userRepository the repository for user data access
     * @param passwordEncoder the encoder for password hashing
     * @param authenticationManager the Spring Security authentication manager
     * @param jwtTokenProvider the JWT token provider for token generation
     */
    @Autowired
    public UserService(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      AuthenticationManager authenticationManager,
                      JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Registers a new user in the system.
     *
     * @param registerRequest the registration request containing user details
     * @return UserResponse containing the registered user information
     * @throws UserAlreadyExistsException if the username or email already exists
     * @throws IllegalArgumentException if the request data is invalid
     */
    public UserResponse register(RegisterRequest registerRequest) {
        logger.info("Attempting to register new user with username: {}", registerRequest.getUsername());

        // Validate input
        validateRegisterRequest(registerRequest);

        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            logger.warn("Registration failed - username already exists: {}", registerRequest.getUsername());
            throw new UserAlreadyExistsException("Username already exists: " + registerRequest.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            logger.warn("Registration failed - email already exists: {}", registerRequest.getEmail());
            throw new UserAlreadyExistsException("Email already exists: " + registerRequest.getEmail());
        }

        // Create new user entity
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setEnabled(true);

        // Save user to database
        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with ID: {}", savedUser.getId());

        return UserResponse.fromUser(savedUser);
    }

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param loginRequest the login request containing credentials
     * @return UserResponse containing user information and JWT token
     * @throws InvalidCredentialsException if authentication fails
     * @throws UserNotFoundException if the user is not found
     */
    public UserResponse login(LoginRequest loginRequest) {
        logger.info("Attempting to login user: {}", loginRequest.getUsername());

        // Validate input
        if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Find user by username
            User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + loginRequest.getUsername()));

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(authentication);
            
            logger.info("User logged in successfully: {}", loginRequest.getUsername());

            UserResponse response = UserResponse.fromUser(user);
            response.setToken(token);
            return response;

        } catch (Exception e) {
            logger.error("Login failed for user: {}", loginRequest.getUsername(), e);
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    /**
     * Retrieves user information by user ID.
     *
     * @param userId the ID of the user to retrieve
     * @return UserResponse containing user information
     * @throws UserNotFoundException if the user is not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        logger.debug("Fetching user by ID: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        return UserResponse.fromUser(user);
    }

    /**
     * Retrieves user information by username.
     *
     * @param username the username of the user to retrieve
     * @return UserResponse containing user information
     * @throws UserNotFoundException if the user is not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        logger.debug("Fetching user by username: {}", username);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        return UserResponse.fromUser(user);
    }

    /**
     * Updates user profile information.
     *
     * @param userId the ID of the user to update
     * @param fullName the new full name (optional)
     * @param email the new email (optional)
     * @return UserResponse containing updated user information
     * @throws UserNotFoundException if the user is not found
     * @throws UserAlreadyExistsException if the email is already taken
     */
    public UserResponse updateUserProfile(Long userId, String fullName, String email) {
        logger.info("Updating profile for user ID: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Update email if provided
        if (email != null && !email.trim().isEmpty()) {
            // Check if email is already taken by another user
            Optional<User> existingUserWithEmail = userRepository.findByEmail(email);
            if (existingUserWithEmail.isPresent() && !existingUserWithEmail.get().getId().equals(userId)) {
                throw new UserAlreadyExistsException("Email already in use: " + email);
            }
            user.setEmail(email);
        }

        // Update full name if provided
        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName);
        }

        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        
        logger.info("Profile updated successfully for user ID: {}", userId);
        return UserResponse.fromUser(updatedUser);
    }

    /**
     * Changes the password for a user.
     *
     * @param userId the ID of the user
     * @param currentPassword the current password for verification
     * @param newPassword the new password to set
     * @throws UserNotFoundException if the user is not found
     * @throws InvalidCredentialsException if the current password is incorrect
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        logger.info("Changing password for user ID: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        // Validate new password
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long");
        }

        // Update password
       