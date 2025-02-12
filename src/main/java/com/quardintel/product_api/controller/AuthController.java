package com.quardintel.product_api.controller;

import com.quardintel.product_api.dto.AuthResponse;
import com.quardintel.product_api.dto.LoginRequest;
import com.quardintel.product_api.dto.RegisterRequest;
import com.quardintel.product_api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * Register a new user and assign the given role
     *
     * @param request Contains user details (username, email, password) and role name
     * @return Success message or error message
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            String message = userService.registerUser(request);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /**
     * Authenticates a user based on the provided credentials.
     *
     * @param request Contains login credentials (username and password).
     * @return ResponseEntity containing authentication details or an error message.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = userService.authenticateUser(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new AuthResponse(ex.getMessage(), null, null, null));
        }
    }
}
