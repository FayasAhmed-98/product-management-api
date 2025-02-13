package com.quardintel.product_api.service;

import com.quardintel.product_api.model.Product;
import com.quardintel.product_api.repository.ProductRepository;
import com.quardintel.product_api.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class); // Logger instance

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Retrieves a product by its ID.
     * Caches the product to improve performance.
     *
     * @param id The ID of the product
     * @return Product object
     */
    @Cacheable(value = "products", key = "#id")
    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    /**
     * Retrieves all products.
     *
     * @return List of all products
     */
    @Cacheable(value = "products", key = "'all'")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Creates a new product.
     *
     * @param product Product data to be saved
     * @return Saved product object
     */
    public Product addProduct(@Valid Product product) {
        Product savedProduct = productRepository.save(product);
        evictAllProductsCache(); // Ensure cache is refreshed after adding new product
        return savedProduct;
    }

    /**
     * Updates an existing product.
     *
     * @param id Product ID to update
     * @param product Updated product data
     * @return Updated product object
     */
    @CacheEvict(value = "products", key = "#id")
    public Product updateProduct(Long id, @Valid Product product) {
        Product existingProduct = getProduct(id); // This will throw an exception if not found
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setQuantity(product.getQuantity());

        Product updatedProduct = productRepository.save(existingProduct);
        evictAllProductsCache(); // Ensure cache is refreshed after update
        return updatedProduct;
    }

    /**
     * Deletes a product.
     *
     * @param id Product ID to delete
     */
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        Product existingProduct = getProduct(id); // This will throw an exception if not found
        productRepository.delete(existingProduct);
        evictAllProductsCache(); // Ensure cache is refreshed after delete
    }

    /**
     * Evicts the 'all' product cache to refresh all product data.
     */
    @CacheEvict(value = "products", key = "'all'", allEntries = true)
    public void evictAllProductsCache() {
        // Log the cache eviction event
        logger.info("Evicting all product cache entries...");
    }
}
