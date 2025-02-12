package com.quardintel.product_api.repository;

import com.quardintel.product_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    // Check if the username already exists
    boolean existsByUsername(String username);

    // Check if the email already exists
    boolean existsByEmail(String email);
}
