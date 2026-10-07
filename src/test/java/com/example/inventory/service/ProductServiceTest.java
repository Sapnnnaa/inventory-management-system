package com.example.inventory.service;


import com.example.inventory.dto.ProductRequest;
import com.example.inventory.entity.Product;
import com.example.inventory.exception.ProductNotFoundException;
import com.example.inventory.kafka.ProductEventProducer;
import com.example.inventory.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductEventProducer productEventProducer;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateProduct(){

        // Arrange
        ProductRequest request = new ProductRequest();

        request.setName("iPhone 15");
        request.setSku("IP15-128");
        request.setDescription("Apple iPhone 15");
        request.setPrice(new BigDecimal("60000"));

        Product savedProduct = new Product();

        savedProduct.setId(1L);
        savedProduct.setName("iPhone 15");
        savedProduct.setSku("IP15-128");
        savedProduct.setDescription("Apple iPhone 15");
        savedProduct.setPrice(new BigDecimal("60000"));

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        // Act
        Product result = productService.createProduct(request);

        // Assert
        assertNotNull(result);

        assertEquals(1L, result.getId());
        assertEquals("iPhone 15", result.getName());
        assertEquals("IP15-128", result.getSku());
        assertEquals(
                new BigDecimal("60000"),
                result.getPrice()
        );

        // verify database save happened
        verify(productRepository, times(1))
                .save(any(Product.class));

        // verify Kafka event was sent
        verify(productEventProducer, times(1))
                .sendProductCreatedEvent(any());


    }

    @Test
    void shouldGetProductById() {

        // Arrange
        Long productId = 1L;

        Product product = new Product();

        product.setId(productId);
        product.setName("iPhone 15");
        product.setSku("IP15-128");
        product.setDescription("Apple iPhone 15");
        product.setPrice(new BigDecimal("60000"));

        when(productRepository.findById(productId))
                .thenReturn(java.util.Optional.of(product));


        // Act
        Product result = productService.getProductById(productId);


        // Assert
        assertNotNull(result);

        assertEquals(1L, result.getId());
        assertEquals("iPhone 15", result.getName());
        assertEquals("IP15-128", result.getSku());
        assertEquals(
                new BigDecimal("60000"),
                result.getPrice()
        );


        // Verify repository was called
        verify(productRepository, times(1))
                .findById(productId);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {

        // Arrange
        Long productId = 999L;

        when(productRepository.findById(productId))
                .thenReturn(java.util.Optional.empty());


        // Act + Assert
        ProductNotFoundException exception =
                assertThrows(
                        ProductNotFoundException.class,
                        () -> productService.getProductById(productId)
                );


        // Verify exception message
        assertEquals(
                "Product not found with id: 999",
                exception.getMessage()
        );


        // Verify repository was called
        verify(productRepository, times(1))
                .findById(productId);
    }

    @Test
    void updateProduct_shouldUpdateAndReturnProduct() {

        // Arrange
        Long productId = 1L;

        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setName("Old Product");
        existingProduct.setSku("OLD-001");
        existingProduct.setDescription("Old Description");
        existingProduct.setPrice(new BigDecimal("1000"));

        ProductRequest request = new ProductRequest();
        request.setName("Updated Product");
        request.setSku("UPD-001");
        request.setDescription("Updated Description");
        request.setPrice(new BigDecimal("2000"));

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(existingProduct));

        when(productRepository.save(existingProduct))
                .thenReturn(existingProduct);

        // Act
        Product result = productService.updateProduct(productId, request);

        // Assert
        assertNotNull(result);

        assertEquals(productId, result.getId());
        assertEquals("Updated Product", result.getName());
        assertEquals("UPD-001", result.getSku());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(
                new BigDecimal("2000"),
                result.getPrice()
        );

        verify(productRepository).findById(productId);
        verify(productRepository).save(existingProduct);
    }

    @Test
    void updateProduct_shouldThrowExceptionWhenProductNotFound() {

        // Arrange
        Long productId = 999L;

        ProductRequest request = new ProductRequest();

        request.setName("Updated Product");
        request.setSku("UPD-001");
        request.setDescription("Updated Description");
        request.setPrice(new BigDecimal("2000"));

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductNotFoundException.class,
                () -> productService.updateProduct(productId, request)
        );

        verify(productRepository).findById(productId);

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void deleteProduct_shouldDeleteProductSuccessfully() {

        Long productId = 1L;

        Product product = new Product();
        product.setId(productId);
        product.setName("iPhone 15");
        product.setSku("IP15-128");
        product.setPrice(new BigDecimal("60000.00"));

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(productId);

        verify(productRepository).findById(productId);
        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_shouldThrowExceptionWhenProductNotFound() {

        Long productId = 999L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.deleteProduct(productId)
        );

        verify(productRepository).findById(productId);
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void searchProducts_shouldReturnMatchingProducts() {

        // Arrange
        String name = "iPhone";
        BigDecimal minPrice = new BigDecimal("50000");
        BigDecimal maxPrice = new BigDecimal("70000");

        Pageable pageable = PageRequest.of(0, 10);

        Product product = new Product();

        product.setId(1L);
        product.setName("iPhone 15");
        product.setSku("IP15-128");
        product.setDescription("Apple iPhone 15");
        product.setPrice(new BigDecimal("60000"));

        Page<Product> productPage =
                new PageImpl<>(
                        List.of(product),
                        pageable,
                        1
                );

        when(productRepository.searchProducts(
                name,
                minPrice,
                maxPrice,
                pageable
        )).thenReturn(productPage);


        // Act
        Page<Product> result =
                productService.searchProducts(
                        name,
                        minPrice,
                        maxPrice,
                        pageable
                );


        // Assert
        assertNotNull(result);

        assertEquals(1, result.getTotalElements());

        assertEquals(
                "iPhone",
                result.getContent().get(0).getName()
                        .replace(" 15", "")
        );

        assertEquals(
                new BigDecimal("60000"),
                result.getContent().get(0).getPrice()
        );


        // Verify repository method was called
        verify(productRepository).searchProducts(
                name,
                minPrice,
                maxPrice,
                pageable
        );
    }

    @Test
    void getAllProducts_shouldReturnPaginatedProducts() {

        // Arrange
        Pageable pageable = PageRequest.of(0, 2);

        Product product1 = new Product();

        product1.setId(1L);
        product1.setName("iPhone 15");
        product1.setSku("IP15-128");
        product1.setPrice(new BigDecimal("60000"));


        Product product2 = new Product();

        product2.setId(2L);
        product2.setName("Samsung S24");
        product2.setSku("S24-256");
        product2.setPrice(new BigDecimal("70000"));


        Page<Product> productPage =
                new PageImpl<>(
                        List.of(product1, product2),
                        pageable,
                        5
                );


        when(productRepository.findAll(pageable))
                .thenReturn(productPage);


        // Act
        Page<Product> result =
                productService.getAllProducts(pageable);


        // Assert
        assertNotNull(result);

        assertEquals(2, result.getContent().size());

        assertEquals(5, result.getTotalElements());

        assertEquals(0, result.getNumber());

        assertEquals(2, result.getSize());


        assertEquals(
                "iPhone 15",
                result.getContent().get(0).getName()
        );

        assertEquals(
                "Samsung S24",
                result.getContent().get(1).getName()
        );


        // Verify repository was called
        verify(productRepository).findAll(pageable);
    }
}
