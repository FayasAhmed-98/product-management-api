package com.quardintel.product_api.integration;

import com.quardintel.product_api.model.Product;
import com.quardintel.product_api.model.Category;
import com.quardintel.product_api.repository.ProductRepository;
import com.quardintel.product_api.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
public class ProductControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        // Ensure the category exists and is not duplicated
        Category category = categoryRepository.findByName("Test Category");
        if (category == null) {
            category = new Category();
            category.setName("Test Category");
            category = categoryRepository.save(category); // Save category to the DB
        }

        // Check if the product already exists, or use a unique name
        Product existingProduct = productRepository.findByName("Test Product");
        if (existingProduct == null) {
            // Prepare sample product data with category
            sampleProduct = new Product();
            sampleProduct.setName("Test Product");
            sampleProduct.setDescription("Test Description");
            sampleProduct.setPrice(100.0);
            sampleProduct.setQuantity(10);

            // Use a Set<Category> instead of List<Category>
            Set<Category> categories = new HashSet<>();
            categories.add(category);  // Add category to the Set
            sampleProduct.setCategories(categories);  // Set categories as a Set

            // Save sample product to the database
            productRepository.save(sampleProduct);
        } else {
            sampleProduct = existingProduct; // If product already exists, use it
        }
    }

    @Test
    void testGetAllProducts() {
        // Send GET request to fetch all products
        ResponseEntity<Product[]> response = restTemplate.getForEntity("/api/products", Product[].class);

        // Validate the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().length);  // Adjust if more products are inserted
    }

    @Test
    void testGetProductById() {
        // Send GET request to fetch the product by ID
        ResponseEntity<Product> response = restTemplate.getForEntity("/api/products/{id}", Product.class, sampleProduct.getId());

        // Validate the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Product", response.getBody().getName());
    }

    @Test
    void testAddProduct() {
        // Create new product
        Product newProduct = new Product();
        newProduct.setName("New Product");
        newProduct.setDescription("New Product Description");
        newProduct.setPrice(200.0);
        newProduct.setQuantity(15);

        // Send POST request to add new product
        ResponseEntity<Product> response = restTemplate.postForEntity("/api/products", newProduct, Product.class);

        // Validate the response
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("New Product", response.getBody().getName());
    }

    @Test
    void testUpdateProduct() {
        // Modify sample product details
        sampleProduct.setName("Updated Product");
        sampleProduct.setDescription("Updated Description");

        // Send PUT request to update product
        restTemplate.put("/api/products/{id}", sampleProduct, sampleProduct.getId());

        // Validate the updated product
        Product updatedProduct = productRepository.findById(sampleProduct.getId()).orElse(null);
        assertEquals("Updated Product", updatedProduct.getName());
        assertEquals("Updated Description", updatedProduct.getDescription());
    }

    @Test
    void testDeleteProduct() {
        // Send DELETE request to delete the product
        restTemplate.delete("/api/products/{id}", sampleProduct.getId());

        // Validate that the product is deleted
        assertEquals(true, productRepository.existsById(sampleProduct.getId()));
    }

    @Test
    @Transactional
    void testSellProduct() {
        int initialQuantity = sampleProduct.getQuantity();

        // Send POST request to sell product (sell 5 units)
        restTemplate.postForEntity("/api/products/{id}/sell/{quantity}", null, Void.class, sampleProduct.getId(), 5);

        // Force reload of the product from the database after transaction
        productRepository.flush();  // Make sure the transaction is committed
        Product updatedProduct = productRepository.findById(sampleProduct.getId()).orElse(null);
        assertNotNull(updatedProduct);  // Ensure product was found
        assertEquals(initialQuantity - 5, updatedProduct.getQuantity());  // Ensure quantity was updated
    }


}
