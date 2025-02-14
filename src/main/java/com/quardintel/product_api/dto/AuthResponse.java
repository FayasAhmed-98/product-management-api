package com.quardintel.product_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponse {
    private String message;
    private String username;
    private String role;
    private String token;

    // Constructor for all fields
    public AuthResponse(String message, String username, String role, String token) {
        this.message = message;
        this.username = username;
        this.role = role;
        this.token = token;
    }

    // Constructor for cases where no token and role are provided
    public AuthResponse(String message, String username) {
        this.message = message;
        this.username = username;
        this.role = null;
        this.token = null;
    }

    // Constructor for error responses (with message only)
    public AuthResponse(String message) {
        this.message = message;
        this.username = null;
        this.role = null;
        this.token = null;
    }
}
