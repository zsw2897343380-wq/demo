package com.example.issue15.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Authentication response DTO.
 * Contains JWT token and user information returned after successful authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "Bearer";

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    @JsonProperty("roles")
    private String roles;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("message")
    private String message;

    @JsonProperty("success")
    @Builder.Default
    private boolean success = true;

    /**
     * Creates a successful authentication response with token and user details.
     *
     * @param accessToken  the JWT access token
     * @param refreshToken the refresh token
     * @param expiresIn    token expiration time in seconds
     * @param userId       the user's ID
     * @param username     the username
     * @param email        the user's email
     * @param roles        the user's roles
     * @return AuthResponse with authentication details
     */
    public static AuthResponse success(String accessToken, String refreshToken, long expiresIn,
                                       Long userId, String username, String email, String roles) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .userId(userId)
                .username(username)
                .email(email)
                .roles(roles)
                .createdAt(LocalDateTime.now())
                .success(true)
                .message("Authentication successful")
                .build();
    }

    /**
     * Creates a failed authentication response.
     *
     * @param message the error message
     * @return AuthResponse with error details
     */
    public static AuthResponse failure(String message) {
        return AuthResponse.builder()
                .success(false)
                .message(message)
                .build();
    }

    /**
     * Creates a response for token refresh.
     *
     * @param accessToken  the new JWT access token
     * @param refreshToken the new refresh token
     * @param expiresIn    token expiration time in seconds
     * @return AuthResponse with refreshed tokens
     */
    public static AuthResponse refreshToken(String accessToken, String refreshToken, long expiresIn) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .success(true)
                .message("Token refreshed successfully")
                .build();
    }

    /**
     * Creates a response for logout.
     *
     * @return AuthResponse indicating successful logout
     */
    public static AuthResponse logout() {
        return AuthResponse.builder()
                .success(true)
                .message("Logged out successfully")
                .build();
    }

    /**
     * Validates that the response contains required fields for a successful authentication.
     *
     * @return true if the response has valid authentication data
     */
    public boolean isValid() {
        if (!success) {
            return true; // Error responses are always valid
        }
        return accessToken != null && !accessToken.isEmpty()
                && userId != null
                && username != null && !username.isEmpty();
    }
}
