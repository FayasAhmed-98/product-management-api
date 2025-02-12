package com.quardintel.product_api.service;

import com.quardintel.product_api.dto.AuthResponse;
import com.quardintel.product_api.dto.LoginRequest;
import com.quardintel.product_api.dto.RegisterRequest;
import com.quardintel.product_api.exception.InvalidCredentialsException;
import com.quardintel.product_api.exception.UserAlreadyExistsException;
import com.quardintel.product_api.model.CustomUserDetails;
import com.quardintel.product_api.model.User;
import com.quardintel.product_api.model.Role;
import com.quardintel.product_api.repository.UserRepository;
import com.quardintel.product_api.repository.RoleRepository;
import com.quardintel.product_api.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;


    /**
     * Registers a new user and assigns the appropriate role
     *
     * @param registerRequest The user details including username, email, and password
     * @return Success message if registration is successful
     */
    public String registerUser(RegisterRequest registerRequest) {
        // Check if the username is already taken
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken.");
        }

        // Check if the email is already in use
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email is already in use.");
        }

        // Determine the role based on the email domain
        String roleName = registerRequest.getEmail().endsWith("@quardintel.com") ? "ADMIN" : "USER";

        // Fetch the Role entity from the database
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        // Create a new User object and set its fields
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword())); // Encrypt the password
        user.setRole(role); // Assign the role

        // Save the User to the database
        userRepository.save(user);

        return "User registered successfully with role: " + role.getName();
    }


    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param authRequest The login request containing username and password.
     * @return An AuthResponse containing the JWT token and user details.
     */
    public AuthResponse authenticateUser(LoginRequest authRequest) {
        // Try to find user by username or email
        User user = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username"));

        // Check if the provided password matches the stored hashed password
        if (!passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        // Generate a JWT token
        String jwt = jwtUtil.generateToken(new CustomUserDetails(user));

        // Return a success response with user details and the token
        return new AuthResponse(
                "Login successful",
                user.getUsername(),
                user.getRole().getName(),
                jwt
        );
    }
}

