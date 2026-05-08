package com.example.issue15.user;

import com.example.issue15.user.dto.LoginRequest;
import com.example.issue15.user.dto.RegisterRequest;
import com.example.issue15.user.dto.UserResponse;
import com.example.issue15.user.exception.UserAlreadyExistsException;
import com.example.issue15.user.exception.UserNotFoundException;
import com.example.issue15.user.exception.InvalidCredentialsException;
import com.example.issue15.user.model.User;
import com.example.issue15.user.service.UserService;
import com.example.issue15.user.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for user management operations.
 * Handles user registration, authentication, and profile management.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * Register a new user account.
     *
     * @param registerRequest the registration details
     * @return ResponseEntity with user details and JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Register the user
            User user = userService.registerUser(registerRequest);
            
            // Generate JWT token
            final UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
            final String token = jwtTokenUtil.generateToken(userDetails);
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("user", new UserResponse(user));
            response.put("token", token);
            response.put("message", "User registered successfully");
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (UserAlreadyExistsException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Registration failed: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Authenticate user and generate JWT token.
     *
     * @param loginRequest the login credentials
     * @return ResponseEntity with JWT token and user details
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            // Generate JWT token
            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            final String token = jwtTokenUtil.generateToken(userDetails);
            
            // Get user details
            User user = userService.getUserByUsername(userDetails.getUsername());
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("user", new UserResponse(user));
            response.put("token", token);
            response.put("message", "Login successful");
            
            return ResponseEntity.ok(response);
            
        } catch (InvalidCredentialsException | AuthenticationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid username or password");
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        } catch (UserNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "User not found");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Login failed: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get current user profile.
     *
     * @param authentication the authenticated user
     * @return ResponseEntity with user details
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            
            return ResponseEntity.ok(new UserResponse(user));
            
        } catch (UserNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "User not found");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve profile: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update user profile information.
     *
     * @param authentication the authenticated user
     * @param updateRequest the updated user details
     * @return ResponseEntity with updated user details
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            Authentication authentication,
            @Valid @RequestBody RegisterRequest updateRequest) {
        try {
            String username = authentication.getName();
            User updatedUser = userService.updateUser(username, updateRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("user", new UserResponse(updatedUser));
            response.put("message", "Profile updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (UserNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "User not found");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update profile: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete user account.
     *
     * @param authentication the authenticated user
     * @return ResponseEntity with deletion status
     */
    @DeleteMapping("/profile")
    public ResponseEntity<?> deleteUserAccount(Authentication authentication) {
        try {
            String username = authentication.getName();
            userService.deleteUser(username);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Account deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (UserNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "User not found");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete account: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Refresh JWT token.
     *
     * @param authentication the authenticated user
     * @return ResponseEntity with new JWT token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(Authentication authentication) {
        try {
            String username = authentication.getName();
            final UserDetails userDetails = userService.loadUserByUsername(username);
            final String newToken = jwtTokenUtil.generateToken(userDetails);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", newToken);
            response.put("message", "Token refreshed successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to refresh token: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
