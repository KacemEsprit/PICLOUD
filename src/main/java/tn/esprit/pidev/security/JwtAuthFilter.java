package tn.esprit.pidev.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    // ...existing code...
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Skip JWT filter for auth endpoints
        String path = request.getServletPath();
        if (path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            
            // Try to extract username (works even if token is expired)
            username = jwtUtil.getUserNameFromJwtTokenIgnoreExpiry(token);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Check if token is still valid (not expired)
                if (jwtUtil.validateJwtToken(token)) {
                    // Token is valid, use it
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    // Refresh token if near expiry (user is active)
                    if (jwtUtil.isTokenNearExpiry(token)) {
                        String newToken = jwtUtil.refreshJwtToken(token);
                        if (newToken != null) {
                            response.setHeader("X-New-Token", newToken);
                        }
                    }
                } else if (jwtUtil.isTokenExpired(token)) {
                    // Token is expired, try to refresh it
                    String newToken = jwtUtil.refreshJwtToken(token);
                    if (newToken != null) {
                        // Successfully refreshed, use the new token
                        response.setHeader("X-New-Token", newToken);
                        
                        // Set authentication with the refreshed token
                        UserDetails refreshedUserDetails = userDetailsService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                refreshedUserDetails, null, refreshedUserDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                    // If refresh fails, don't set authentication (user will get 401)
                }
            } catch (Exception e) {
                logger.error("Cannot set user authentication: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}


