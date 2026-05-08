package com.example.issue15.user.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for User entity.
 * Used for transferring user data between layers and API communication.
 */
public class UserDto {

    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    private String firstName;

    private String lastName;

    private boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

    /**
     * Default constructor.
     */
    public UserDto() {
    }

    /**
     * Constructor with required fields.
     *
     * @param username the username
     * @param email    the email
     * @param password the password
     */
    public UserDto(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    /**
     * Full constructor.
     *
     * @param id          the user ID
     * @param username    the username
     * @param email       the email
     * @param password    the password
     * @param firstName   the first name
     * @param lastName    the last name
     * @param enabled     whether the user is enabled
     * @param createdAt   the creation timestamp
     * @param updatedAt   the update timestamp
     * @param lastLoginAt the last login timestamp
     */
    public UserDto(Long id, String username, String email, String password,
                   String firstName, String lastName, boolean enabled,
                   LocalDateTime createdAt, LocalDateTime updatedAt,
                   LocalDateTime lastLoginAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    /**
     * Returns a string representation of the UserDto.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "UserDto{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", enabled=" + enabled +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", lastLoginAt=" + lastLoginAt +
                '}';
    }

    /**
     * Checks if this UserDto is equal to another object.
     *
     * @param o the object to compare
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserDto userDto = (UserDto) o;

        if (enabled != userDto.enabled) return false;
        if (id != null ? !id.equals(userDto.id) : userDto.id != null) return false;
        if (username != null ? !username.equals(userDto.username) : userDto.username != null) return false;
        if (email != null ? !email.equals(userDto.email) : userDto.email != null) return false;
        if (firstName != null ? !firstName.equals(userDto.firstName) : userDto.firstName != null) return false;
        if (lastName != null ? !lastName.equals(userDto.lastName) : userDto.lastName != null) return false;
        if (createdAt != null ? !createdAt.equals(userDto.createdAt) : userDto.createdAt != null) return false;
        if (updatedAt != null ? !updatedAt.equals(userDto.updatedAt) : userDto.updatedAt != null) return false;
        return lastLoginAt != null ? lastLoginAt.equals(userDto.lastLoginAt) : userDto.lastLoginAt == null;
    }

    /**
     * Returns a hash code value for this UserDto.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (updatedAt != null ? updatedAt.hashCode() : 0);
        result = 31 * result + (lastLoginAt != null ? lastLoginAt.hashCode() : 0);
        return result;
    }
}
