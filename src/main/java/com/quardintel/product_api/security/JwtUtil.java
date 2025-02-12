package com.quardintel.product_api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utility class to generate and validate JWT tokens.
 */
@Component
public class JwtUtil {

    // Secret key used for signing JWT tokens
    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    /**
     * Generates a JWT token for the given user details.
     *
     * @param userDetails UserDetails containing user information (e.g., username).
     * @return A signed JWT token.
     */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour expiration
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * Validates the JWT token by checking its expiration and username.
     *
     * @param token JWT token.
     * @param userDetails UserDetails containing user information.
     * @return True if the token is valid, false otherwise.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Extracts the username (subject) from the JWT token.
     *
     * @param token JWT token.
     * @return Username (subject) from the token.
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts the expiration date from the JWT token.
     *
     * @param token JWT token.
     * @return Expiration date of the token.
     */
    private Date extractExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    /**
     * Parses the JWT token and retrieves the claims (payload).
     *
     * @param token JWT token.
     * @return Claims of the token.
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token has expired", e);
        } catch (JwtException e) {
            throw new RuntimeException("Token parsing failed", e);
        }
    }

    /**
     * Checks if the JWT token has expired.
     *
     * @param token JWT token.
     * @return True if the token is expired, false otherwise.
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    // Validate the token
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
}
