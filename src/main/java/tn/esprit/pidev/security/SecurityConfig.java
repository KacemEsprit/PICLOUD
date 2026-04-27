package tn.esprit.pidev.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

/**
 * Security Configuration with Role-Based Access Control (RBAC)
 *
 * Role Hierarchy:
 * - ADMIN: Full access to admin endpoints, user management, document approval
 * - AGENT: Can view and manage agent-specific documents
 * - OPERATOR: Can view and manage operator-specific documents
 * - PASSENGER: Can upload and view personal documents
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ============================================
                        // SWAGGER/OPENAPI ENDPOINTS - No authentication required
                        // ============================================
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()

                        // ============================================
                        // PUBLIC ENDPOINTS - No authentication required
                        // ============================================
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/forget-password").permitAll()
                        .requestMatchers("/api/auth/reset-password").permitAll()
                        .requestMatchers("/health", "/actuator/**").permitAll()

                        // Static files served from htdocs (accessible to all)
                        .requestMatchers("/pidev-uploads/**").permitAll()

                        // ============================================
                        // AUTHENTICATED ENDPOINTS - All authenticated users
                        // ============================================
                        .requestMatchers("/api/auth/refresh").authenticated()
                        .requestMatchers("/api/profile/**").authenticated()
                        .requestMatchers("/api/documents/**").authenticated()
                        .requestMatchers("GET", "/api/document-types").authenticated()
                        .requestMatchers("GET", "/api/document-types/**").authenticated()

                        // ============================================
                        // ADMIN ENDPOINTS - ADMIN role only
                        // ============================================
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Admin User Management
                        .requestMatchers("/api/admin/users/**").hasRole("ADMIN")

                        // Admin Document Management
                        .requestMatchers("/api/admin/documents/**").hasRole("ADMIN")
                        .requestMatchers("POST", "/api/admin/documents/{id}/approve").hasRole("ADMIN")
                        .requestMatchers("POST", "/api/admin/documents/{id}/reject").hasRole("ADMIN")
                        .requestMatchers("POST", "/api/admin/documents/{id}/request-update").hasRole("ADMIN")

                        // Admin Document Type Management
                        .requestMatchers("/api/admin/document-types/**").hasRole("ADMIN")
                        .requestMatchers("POST", "/api/admin/document-types").hasRole("ADMIN")
                        .requestMatchers("PUT", "/api/admin/document-types/**").hasRole("ADMIN")
                        .requestMatchers("DELETE", "/api/admin/document-types/**").hasRole("ADMIN")

                        // ============================================
                        // ROLE-SPECIFIC ENDPOINTS
                        // ============================================
                        // AGENT endpoints
                        .requestMatchers("/api/agent/**").hasRole("AGENT")

                        // OPERATOR endpoints
                        .requestMatchers("/api/operator/**").hasRole("OPERATOR")

                        // PASSENGER endpoints
                        .requestMatchers("/api/passenger/**").hasRole("PASSENGER")

                        // ============================================
                        // MULTI-ROLE ENDPOINTS - Multiple roles allowed
                        // ============================================
                        // Users (non-admin) can view their own profile
                        .requestMatchers("GET", "/api/profile").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")
                        .requestMatchers("PUT", "/api/profile").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")
                        .requestMatchers("POST", "/api/profile/photo").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")
                        .requestMatchers("DELETE", "/api/profile/photo").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")
                        .requestMatchers("GET", "/api/profile/photo").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")

                        // Users (non-admin) can manage their documents
                        .requestMatchers("GET", "/api/documents").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")
                        .requestMatchers("GET", "/api/documents/**").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")
                        .requestMatchers("POST", "/api/documents").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")
                        .requestMatchers("DELETE", "/api/documents/**").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")
                        .requestMatchers("POST", "/api/documents/{id}/reupload").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")
                        .requestMatchers("GET", "/api/documents/{id}/download").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")

                        // ============================================
                        // NOTIFICATIONS & INCIDENTS - Authenticated users
                        // ============================================
                        .requestMatchers("GET", "/notifications/my").authenticated()
                        .requestMatchers("GET", "/notifications").authenticated()
                        .requestMatchers("GET", "/notifications/**").authenticated()
                        .requestMatchers("POST", "/notifications/**").authenticated()
                        .requestMatchers("PUT", "/notifications/**").authenticated()
                        .requestMatchers("DELETE", "/notifications/**").authenticated()
                        .requestMatchers("PATCH", "/notifications/**").authenticated()
                        
                        .requestMatchers("GET", "/incidents").authenticated()
                        .requestMatchers("GET", "/incidents/**").authenticated()
                        .requestMatchers("POST", "/incidents/**").authenticated()
                        .requestMatchers("PUT", "/incidents/**").authenticated()
                        .requestMatchers("DELETE", "/incidents/**").authenticated()

                        // ============================================
                        // DEFAULT - Deny all other requests
                        // ============================================
                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}