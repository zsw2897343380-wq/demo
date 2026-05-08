package com.example.issue15.auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter that intercepts incoming requests and validates JWT tokens.
 * This filter extends OncePerRequestFilter to ensure it's executed once per request.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * Constructor for JwtAuthenticationFilter.
     *
     * @param jwtTokenProvider    the JWT token provider for token operations
     * @param userDetailsService  the user details service for loading user information
     */
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Main filter method that processes each incoming request.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain for continuing the request processing
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Extract JWT token from the request header
            String jwt = extractJwtFromRequest(request);

            // Validate token and set authentication context
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                String username = jwtTokenProvider.getUsernameFromToken(jwt);

                // Load user details and create authentication object
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set the authentication in the security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT token has expired");
            return;
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT token is unsupported");
            return;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token format: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token format");
            return;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT signature");
            return;
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT token is missing or invalid");
            return;
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication processing failed");
            return;
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT token from the Authorization header.
     * The expected format is: "Bearer <token>"
     *
     * @param request the HTTP request
     * @return the JWT token string, or null if not found
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Check if the header contains a Bearer token
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Extract the token part (remove "Bearer " prefix)
            return bearerToken.substring(7);
        }

        return null;
    }

    /**
     * Determines if this filter should be applied to the current request.
     * Skips authentication for public endpoints like login and registration.
     *
     * @param request the HTTP request
     * @return true if the filter should not be applied, false otherwise
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Skip authentication for public endpoints
        return path.equals("/api/auth/login") ||
               path.equals("/api/auth/register") ||
               path.startsWith("/api/auth/refresh") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/actuator");
    }
}
