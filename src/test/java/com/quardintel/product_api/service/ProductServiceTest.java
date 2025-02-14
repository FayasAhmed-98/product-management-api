package com.quardintel.product_api.service;

import com.quardintel.product_api.model.Product;
import com.quardintel.product_api.model.Category;
import com.quardintel.product_api.repository.ProductRepository;
import com.quardintel.product_api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Spy;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Spy
    @InjectMocks
    private ProductService productService;  // Use @Spy for ProductService to spy on its internal methods

    private Product sampleProduct;
    private Category category1, category2;

    @BeforeEach
    void setUp() {
        // Creating sample categories
        category1 = new Category();
        category1.setId(1L);
        category1.setName("Electronics");

        category2 = new Category();
        category2.setId(2L);
        category2.setName("Home Appliances");

        // Creating a sample product with one category
        Set<Category> categories = new HashSet<>();
        categories.add(category1);

        sampleProduct = new Product();
        sampleProduct.setId(1L);
        sampleProduct.setName("Test Product");
        sampleProduct.setDescription("Test Description");
        sampleProduct.setPrice(100.0);
        sampleProduct.setQuantity(10);
        sampleProduct.setCategories(categories);
    }

    /**
     * Test: Fetch product by ID (Successful)
     */
    @Test
    void testGetProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        Product result = productService.getProduct(1L);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals(1, result.getCategories().size());

        verify(productRepository, times(1)).findById(1L);
    }

    /**
     * Test: Fetch product by ID (Not Found)
     */
    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProduct(2L));

        verify(productRepository, times(1)).findById(2L);
    }

    /**
     * Test: Create a new product
     */
    @Test
    void testAddProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        Product result = productService.addProduct(sampleProduct);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productRepository, times(1)).save(sampleProduct);
        // Verifying cache eviction is called (if you are using a cache library like Spring's @CacheEvict)
        verify(productService, times(1)).evictAllProductsCache();  // Ensure this is working with spy
    }

    /**
     * Test: Update existing product details & categories
     */
    @Test
    void testUpdateProduct_WithCategories() {
        // Creating updated product data
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Product");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setPrice(150.0);
        updatedProduct.setQuantity(5);

        // Adding a new category
        Set<Category> updatedCategories = new HashSet<>();
        updatedCategories.add(category1);
        updatedCategories.add(category2);
        updatedProduct.setCategories(updatedCategories);

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // Perform update
        Product result = productService.updateProduct(1L, updatedProduct);

        // Assertions
        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
        assertEquals(2, result.getCategories().size());
        assertTrue(result.getCategories().contains(category1));
        assertTrue(result.getCategories().contains(category2));

        verify(productRepository, times(1)).save(any(Product.class));
        verify(productService, times(1)).evictAllProductsCache();  // Ensure this is working with spy
    }

    /**
     * Test: Delete a product
     */
    @Test
    void testDeleteProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        doNothing().when(productRepository).delete(sampleProduct);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).delete(sampleProduct);
        verify(productService, times(1)).evictAllProductsCache();  // Ensure this is working with spy
    }

    /**
     * Test: Sell product (Reduce stock)
     */
    @Test
    void testSellProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        productService.sellProduct(1L, 5);

        assertEquals(5, sampleProduct.getQuantity());
        verify(productRepository, times(1)).save(sampleProduct);
        verify(productService, times(1)).evictAllProductsCache();  // Ensure this is working with spy
    }

    /**
     * Test: Sell product with insufficient stock
     */
    @Test
    void testSellProduct_InsufficientStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        assertThrows(IllegalArgumentException.class, () -> productService.sellProduct(1L, 20));

        verify(productRepository, never()).save(sampleProduct);
    }
}
