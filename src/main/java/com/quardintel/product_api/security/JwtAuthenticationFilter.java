package com.quardintel.product_api.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    private final JwtUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtil jwtTokenUtil, UserDetailsService userDetailsService) {
        super(authenticationManager);
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Retrieve the Authorization header from the request
        String header = request.getHeader("Authorization");

        // Check if the Authorization header is missing or does not start with "Bearer "
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response); // Proceed to the next filter in the chain
            return;
        }

        // Extract the token from the Authorization header (skip "Bearer " prefix)
        String token = header.substring(7);

        try {
            // Extract username from the JWT token
            String username = jwtTokenUtil.extractUsername(token);

            // Authenticate only if the token is valid and no authentication is already set
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Load user details from the UserDetailsService
                var userDetails = userDetailsService.loadUserByUsername(username);

                // Validate the token against the extracted user details
                if (jwtTokenUtil.isTokenValid(token, userDetails)) {

                    // Create an authentication object with user details and authorities
                    var authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // No credentials are needed since authentication is via JWT
                            userDetails.getAuthorities()
                    );

                    // Set the authentication in the SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (ExpiredJwtException e) {
            // Handle expired token case
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token expired");
            return;
        } catch (JwtException | IllegalArgumentException e) {
            // Handle invalid token case
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token");
            return;
        }

        // Continue with the next filter in the chain
        chain.doFilter(request, response);
    }

}
