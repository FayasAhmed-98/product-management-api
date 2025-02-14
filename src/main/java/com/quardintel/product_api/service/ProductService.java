package com.quardintel.product_api.service;

import com.quardintel.product_api.model.Product;
import com.quardintel.product_api.repository.ProductRepository;
import com.quardintel.product_api.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        logger.info("Fetching product with ID: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    /**
     * Retrieves all products.
     * Caches the list of all products.
     *
     * @return List of all products
     */
    @Cacheable(value = "products", key = "'all_products'")
    public List<Product> getAllProducts() {
        logger.info("Fetching all products");
        return productRepository.findAll();
    }

    /**
     * Creates a new product and ensures cache is refreshed.
     *
     * @param product Product data to be saved
     * @return Saved product object
     */
    @Transactional
    public Product addProduct(@Valid Product product) {
        logger.info("Adding new product: {}", product.getName());
        Product savedProduct = productRepository.save(product);
        evictAllProductsCache();
        return savedProduct;
    }

    /**
     * Updates an existing product, including its associated categories.
     *
     * @param id       Product ID to update
     * @param product  Updated product data (including categories)
     * @return Updated product object
     */
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public Product updateProduct(Long id, Product product) {
        logger.info("Updating product with ID: {}", id);
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setQuantity(product.getQuantity());

        // Update categories if they are present
        if (product.getCategories() != null && !product.getCategories().isEmpty()) {
            existingProduct.setCategories(product.getCategories());
            logger.info("Updated categories for product ID: {}", id);
        }

        Product updatedProduct = productRepository.save(existingProduct);
        evictAllProductsCache();
        return updatedProduct;
    }

    /**
     * Deletes a product and ensures cache consistency.
     *
     * @param id Product ID to delete
     */
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public boolean deleteProduct(Long id) {
        logger.info("Deleting product with ID: {}", id);
        Product existingProduct = productRepository.findById(id)
                .orElse(null);

        if (existingProduct == null) {
            return false;  // Product not found, cannot delete
        }

        productRepository.delete(existingProduct);
        evictAllProductsCache();
        return true;
    }


    /**
     * Handles selling a product and updates inventory.
     * Prevents selling more than available stock.
     *
     * @param id Product ID
     * @param quantitySold Quantity to be sold
     */
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void sellProduct(Long id, int quantitySold) {
        logger.info("Processing sale for product ID: {} - Quantity Sold: {}", id, quantitySold);

        if (quantitySold <= 0) {
            throw new IllegalArgumentException("Sale quantity must be greater than zero.");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        if (product.getQuantity() < quantitySold) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }

        product.setQuantity(product.getQuantity() - quantitySold);
        productRepository.save(product);
        evictAllProductsCache();
    }
    /**
     * Evicts all cached product entries to maintain data consistency.
     */
    @CacheEvict(value = "products", allEntries = true)
    public void evictAllProductsCache() {
        logger.info("Evicting all product cache entries...");
    }
}
