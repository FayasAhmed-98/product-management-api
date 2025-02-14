package com.quardintel.product_api.controller;

import com.quardintel.product_api.model.Product;
import com.quardintel.product_api.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Get all products (accessible by both Admin and User)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();  // Return 204 if no products are found
        }
        return ResponseEntity.ok(products);
    }

    // Get a product by ID (accessible by both Admin and User)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        Product product = productService.getProduct(id);
        if (product == null) {
            return ResponseEntity.notFound().build();  // Return 404 if product is not found
        }
        return ResponseEntity.ok(product);
    }

    // Create a new product (only Admin)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Product> addProduct(@Valid @RequestBody Product product) {
        Product createdProduct = productService.addProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    // Update an existing product (only Admin)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        Product updatedProduct = productService.updateProduct(id, product);
        if (updatedProduct == null) {
            return ResponseEntity.notFound().build();  // Return 404 if product to update is not found
        }
        return ResponseEntity.ok(updatedProduct);
    }

    // Delete a product (only Admin)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        boolean isDeleted = productService.deleteProduct(id);
        if (!isDeleted) {
            return ResponseEntity.notFound().build();  // Return 404 if product not found for deletion
        }
        return ResponseEntity.noContent().build();
    }

    // Sell product (only Admin)
    @PostMapping("/{id}/sell/{quantity}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> sellProduct(@PathVariable Long id, @PathVariable int quantity) {
        try {
            productService.sellProduct(id, quantity);
            return ResponseEntity.ok().build();  // Return a successful response
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);  // Return 400 if the sale is invalid
        }
    }
}
