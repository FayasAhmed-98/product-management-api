package com.quardintel.product_api.config;

import com.quardintel.product_api.exception.CustomAccessDeniedHandler;
import com.quardintel.product_api.exception.CustomAuthenticationEntryPoint;
import com.quardintel.product_api.security.JwtAuthenticationFilter;
import com.quardintel.product_api.security.JwtUtil;
import com.quardintel.product_api.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtUtil jwtTokenUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    // Constructor injection
    public SecurityConfig(JwtUtil jwtTokenUtil,
                          CustomUserDetailsService customUserDetailsService, CustomAuthenticationEntryPoint customAuthenticationEntryPoint, CustomAccessDeniedHandler customAccessDeniedHandler) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.customUserDetailsService = customUserDetailsService;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers("/auth/login", "/auth/register").permitAll()// Allow public access to login & registration
                // Role-based access control using hasRole
                .requestMatchers(HttpMethod.GET, "/api/products/**").hasAnyRole("USER", "ADMIN") // Users and Admins can view products
                .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN") // Only Admins can create products
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN") // Only Admins can update products
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN") // Only Admins can delete products
                .anyRequest().authenticated() // Secure all other endpoints
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(customAuthenticationEntryPoint) // Custom entry point for unauthenticated users
                .accessDeniedHandler(customAccessDeniedHandler)           // Custom handler for access denied
                .and()
                .addFilterBefore(new JwtAuthenticationFilter(
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
