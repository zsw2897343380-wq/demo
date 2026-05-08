package com.example.issue15.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * Application configuration properties.
 * <p>
 * This class maps the application configuration from application.yml or application.properties
 * to a type-safe Java object. It provides centralized access to JWT settings,
 * security configurations, and other application-specific properties.
 * </p>
 *
 * <p>
 * Configuration prefix: "app"
 * </p>
 *
 * <p>
 * Example application.yml configuration:
 * <pre>
 * app:
 *   jwt:
 *     secret: your-secret-key-here
 *     expiration-ms: 86400000
 *     issuer: issue15-app
 *   security:
 *     allowed-origins: http://localhost:3000
 *     max-login-attempts: 5
 *     lockout-duration-minutes: 30
 * </pre>
 * </p>
 *
 * @author Issue15 Team
 * @version 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "app")
@Validated
public class ApplicationProperties {

    /**
     * JWT configuration properties.
     */
    @NotNull(message = "JWT configuration must not be null")
    private Jwt jwt = new Jwt();

    /**
     * Security configuration properties.
     */
    @NotNull(message = "Security configuration must not be null")
    private Security security = new Security();

    /**
     * Gets the JWT configuration.
     *
     * @return the JWT configuration
     */
    public Jwt getJwt() {
        return jwt;
    }

    /**
     * Sets the JWT configuration.
     *
     * @param jwt the JWT configuration to set
     */
    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    /**
     * Gets the security configuration.
     *
     * @return the security configuration
     */
    public Security getSecurity() {
        return security;
    }

    /**
     * Sets the security configuration.
     *
     * @param security the security configuration to set
     */
    public void setSecurity(Security security) {
        this.security = security;
    }

    /**
     * JWT configuration properties class.
     * <p>
     * Contains settings for JWT token generation, validation, and management.
     * </p>
     */
    public static class Jwt {

        /**
         * Secret key used for signing JWT tokens.
         * Must be at least 256 bits for HS256 algorithm.
         */
        @NotBlank(message = "JWT secret must not be blank")
        private String secret;

        /**
         * Token expiration time in milliseconds.
         * Default value: 24 hours (86400000 ms).
         */
        @Positive(message = "JWT expiration must be positive")
        private long expirationMs = 86400000L;

        /**
         * Token issuer claim value.
         */
        @NotBlank(message = "JWT issuer must not be blank")
        private String issuer = "issue15-app";

        /**
         * Gets the JWT secret key.
         *
         * @return the secret key
         */
        public String getSecret() {
            return secret;
        }

        /**
         * Sets the JWT secret key.
         *
         * @param secret the secret key to set
         */
        public void setSecret(String secret) {
            this.secret = secret;
        }

        /**
         * Gets the token expiration time in milliseconds.
         *
         * @return the expiration time in milliseconds
         */
        public long getExpirationMs() {
            return expirationMs;
        }

        /**
         * Sets the token expiration time in milliseconds.
         *
         * @param expirationMs the expiration time to set
         */
        public void setExpirationMs(long expirationMs) {
            this.expirationMs = expirationMs;
        }

        /**
         * Gets the token issuer.
         *
         * @return the issuer
         */
        public String getIssuer() {
            return issuer;
        }

        /**
         * Sets the token issuer.
         *
         * @param issuer the issuer to set
         */
        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        @Override
        public String toString() {
            return "Jwt{" +
                    "secret='[PROTECTED]'" +
                    ", expirationMs=" + expirationMs +
                    ", issuer='" + issuer + '\'' +
                    '}';
        }
    }

    /**
     * Security configuration properties class.
     * <p>
     * Contains settings for application security, including CORS, rate limiting,
     * and account lockout policies.
     * </p>
     */
    public static class Security {

        /**
         * Allowed origins for CORS configuration.
         * Default value: localhost:3000 (React development server).
         */
        @NotBlank(message = "Allowed origins must not be blank")
        private String allowedOrigins = "http://localhost:3000";

        /**
         * Maximum number of failed login attempts before account lockout.
         * Default value: 5 attempts.
         */
        @Positive(message = "Max login attempts must be positive")
        private int maxLoginAttempts = 5;

        /**
         * Duration in minutes for account lockout after exceeding max login attempts.
         * Default value: 30 minutes.
         */
        @Positive(message = "Lockout duration must be positive")
        private int lockoutDurationMinutes = 30;

        /**
         * Gets the allowed origins for CORS.
         *
         * @return the allowed origins
         */
        public String getAllowedOrigins() {
            return allowedOrigins;
        }

        /**
         * Sets the allowed origins for CORS.
         *
         * @param allowedOrigins the allowed origins to set
         */
        public void setAllowedOrigins(String allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        /**
         * Gets the maximum number of failed login attempts.
         *
         * @return the maximum login attempts
         */
        public int getMaxLoginAttempts() {
            return maxLoginAttempts;
        }

        /**
         * Sets the maximum number of failed login attempts.
         *
         * @param maxLoginAttempts the maximum login attempts to set
         */
        public void setMaxLoginAttempts(int maxLoginAttempts) {
            this.maxLoginAttempts = maxLoginAttempts;
        }

        /**
         * Gets the lockout duration in minutes.
         *
         * @return the lockout duration in minutes
         */
        public int getLockoutDurationMinutes() {
            return lockoutDurationMinutes;
        }

        /**
         * Sets the lockout duration in minutes.
         *
         * @param lockoutDurationMinutes the lockout duration to set
         */
        public void setLockoutDurationMinutes(int lockoutDurationMinutes) {
            this.lockoutDurationMinutes = lockoutDurationMinutes;
        }

        @Override
        public String toString() {
            return "Security{" +
                    "allowedOrigins='" + allowedOrigins + '\'' +
                    ", maxLoginAttempts=" + maxLoginAttempts +
                    ", lockoutDurationMinutes=" + lockoutDurationMinutes +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ApplicationProperties{" +
                "jwt=" + jwt +
                ", security=" + security +
                '}';
    }
}
