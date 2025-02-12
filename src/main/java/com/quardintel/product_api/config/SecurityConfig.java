package com.quardintel.product_api.config;

import com.quardintel.product_api.security.JwtAuthenticationFilter;
import com.quardintel.product_api.security.JwtUtil;
import com.quardintel.product_api.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    private final JwtUtil jwtTokenUtil;
    private final CustomUserDetailsService customUserDetailsService;

    // Constructor injection
    public SecurityConfig(JwtUtil jwtTokenUtil,
                          CustomUserDetailsService customUserDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for REST APIs
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No sessions
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/api/public/**", // Public APIs
                                "/auth/register",  // Allow registration without authentication
                                "/auth/login"      // Allow login without authentication
                        ).permitAll()
                        .anyRequest().authenticated() // Secure all other endpoints
                );

        // Add the JWT authentication filter
        http.addFilterBefore(new JwtAuthenticationFilter(
                authenticationManager(http.getSharedObject(AuthenticationConfiguration.class)),
                jwtTokenUtil,
                customUserDetailsService
        ), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
