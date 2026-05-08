package com.example.issue15.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * Provides CRUD operations and custom queries for user management.
 * 
 * This repository supports:
 * - User registration and authentication
 * - JWT token management
 * - User profile information management
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username for authentication purposes.
     *
     * @param username the username to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email for authentication and profile management.
     *
     * @param email the email address to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username already exists in the system.
     *
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if email already exists in the system.
     *
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find user by JWT token for token-based authentication.
     *
     * @param token the JWT token to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByJwtToken(String token);

    /**
     * Update user's last login timestamp.
     *
     * @param userId the ID of the user
     * @param lastLogin the timestamp of the last login
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("lastLogin") LocalDateTime lastLogin);

    /**
     * Update user's JWT token for session management.
     *
     * @param userId the ID of the user
     * @param token the new JWT token
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.jwtToken = :token WHERE u.id = :userId")
    void updateJwtToken(@Param("userId") Long userId, @Param("token") String token);

    /**
     * Update user's password for security purposes.
     *
     * @param userId the ID of the user
     * @param newPassword the new encrypted password
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.password = :newPassword WHERE u.id = :userId")
    void updatePassword(@Param("userId") Long userId, @Param("newPassword") String newPassword);

    /**
     * Update user's profile information.
     *
     * @param userId the ID of the user
     * @param email the new email address
     * @param phoneNumber the new phone number
     * @param fullName the new full name
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.email = :email, u.phoneNumber = :phoneNumber, " +
           "u.fullName = :fullName, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void updateProfile(@Param("userId") Long userId, 
                      @Param("email") String email,
                      @Param("phoneNumber") String phoneNumber, 
                      @Param("fullName") String fullName);

    /**
     * Soft delete a user account by setting the deleted flag.
     *
     * @param userId the ID of the user to deactivate
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isActive = false, u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void softDeleteUser(@Param("userId") Long userId);

    /**
     * Find active user by ID (excludes soft-deleted users).
     *
     * @param id the user ID
     * @return Optional containing the active user if found, empty otherwise
     */
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isActive = true")
    Optional<User> findActiveById(@Param("id") Long id);

    /**
     * Find active user by username (excludes soft-deleted users).
     *
     * @param username the username
     * @return Optional containing the active user if found, empty otherwise
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.isActive = true")
    Optional<User> findActiveByUsername(@Param("username") String username);

    /**
     * Find active user by email (excludes soft-deleted users).
     *
     * @param email the email address
     * @return Optional containing the active user if found, empty otherwise
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveByEmail(@Param("email") String email);

    /**
     * Count total active users in the system.
     *
     * @return the count of active users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();

    /**
     * Find all users created after a specific date.
     *
     * @param dateTime the date threshold
     * @return list of users created after the specified date
     */
    @Query("SELECT u FROM User u WHERE u.createdAt > :dateTime AND u.isActive = true")
    java.util.List<User> findUsersCreatedAfter(@Param("dateTime") LocalDateTime dateTime);

    /**
     * Search users by username or email (case-insensitive).
     *
     * @param searchTerm the search term
     * @return list of matching users
     */
    @Query("SELECT u FROM User u WHERE (LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND u.isActive = true")
    java.util.List<User> searchUsers(@Param("searchTerm") String searchTerm);
}
