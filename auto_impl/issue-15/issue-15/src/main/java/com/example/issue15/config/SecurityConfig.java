package com.example.issue15.config;

import com.example.issue15.security.JwtAuthenticationFilter;
import com.example.issue15.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration class.
 * Configures JWT authentication, security rules, CORS, and password encoding.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Constructor injection for JwtTokenProvider.
     *
     * @param jwtTokenProvider the JWT token provider
     */
    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Configures the security filter chain.
     *
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF protection for stateless JWT authentication
            .csrf(csrf -> csrf.disable())
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure session management to be stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                
                // User management endpoints
                .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                
                // Admin only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Add JWT authentication filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    /**
     * Creates the JWT authentication filter bean.
     *
     * @return the JwtAuthenticationFilter instance
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }

    /**
     * Creates the authentication manager bean.
     *
     * @param authenticationConfiguration the authentication configuration
     * @return the AuthenticationManager instance
     * @throws Exception if authentication manager creation fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) 
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Creates the password encoder bean using BCrypt hashing algorithm.
     *
     * @return the PasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures CORS settings to allow cross-origin requests.
     *
     * @return the CorsConfigurationSource instance
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins (configure based on your frontend URL)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:8080",
            "https://your-frontend-domain.com"
        ));
        
        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.PATCH.name(),
            HttpMethod.OPTIONS.name()
        ));
        
        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Expose the Authorization header
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Disposition"
        ));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Set max age for preflight requests (1 hour)
        configuration.setMaxAge(3600L);
        
        // Apply CORS configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
