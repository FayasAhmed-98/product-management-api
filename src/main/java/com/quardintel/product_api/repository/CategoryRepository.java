package com.quardintel.product_api.repository;

import com.quardintel.product_api.model.Category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Custom query method to find a category by its name
    Category findByName(String name);
}
