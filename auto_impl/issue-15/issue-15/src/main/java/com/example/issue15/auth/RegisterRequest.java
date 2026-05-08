package com.example.issue15.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for user registration.
 * Contains all necessary fields for creating a new user account.
 */
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    /**
     * Default constructor.
     */
    public RegisterRequest() {
    }

    /**
     * Parameterized constructor for convenient object creation.
     *
     * @param username the desired username
     * @param email    the user's email address
     * @param password the user's password
     * @param fullName the user's full name (optional)
     */
    public RegisterRequest(String username, String email, String password, String fullName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address.
     *
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the full name.
     *
     * @return the full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the full name.
     *
     * @param fullName the full name to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Returns a string representation of the RegisterRequest object.
     * Note: Password is masked for security purposes.
     *
     * @return a string representation of this request
     */
    @Override
    public String toString() {
        return "RegisterRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                ", fullName='" + fullName + '\'' +
                '}';
    }

    /**
     * Checks if this RegisterRequest is equal to another object.
     *
     * @param o the object to compare with
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegisterRequest that = (RegisterRequest) o;

        if (!username.equals(that.username)) return false;
        if (!email.equals(that.email)) return false;
        if (!password.equals(that.password)) return false;
        return fullName != null ? fullName.equals(that.fullName) : that.fullName == null;
    }

    /**
     * Generates a hash code for this RegisterRequest.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + (fullName != null ? fullName.hashCode() : 0);
        return result;
    }
}
