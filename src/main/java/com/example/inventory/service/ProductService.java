package com.example.inventory.service;


import com.example.inventory.dto.ProductRequest;
import com.example.inventory.entity.Product;
import com.example.inventory.exception.ProductNotFoundException;
import com.example.inventory.kafka.ProductEvent;
import com.example.inventory.kafka.ProductEventProducer;
import com.example.inventory.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final ProductEventProducer productEventProducer;

    public ProductService(
            ProductRepository productRepository,
            ProductEventProducer productEventProducer) {

        this.productRepository = productRepository;
        this.productEventProducer = productEventProducer;
    }

    public Product createProduct(ProductRequest request) {

        Product product = new Product();

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());

        Product savedProduct = productRepository.save(product);

        ProductEvent event = new ProductEvent(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getSku(),
                savedProduct.getPrice()
        );

        productEventProducer.sendProductCreatedEvent(event);

        return savedProduct;
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }


    @Cacheable(value = "products", key = "#id")
    public Product getProductById(Long id) {

        System.out.println("Fetching product from database...");

        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id
                        )
                );
    }

    @CachePut(value = "products", key = "#id")
    public Product updateProduct(Long id, ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id
                        )
                );

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());

        return productRepository.save(product);
    }


    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id
                        )
                );

        productRepository.delete(product);
    }

//    public List<Product> searchProducts(String name) {
//        return productRepository.findByNameContainingIgnoreCase(name);
//    }

//    public List<Product> filterProductsByPrice(
//            BigDecimal minPrice,
//            BigDecimal maxPrice) {
//
//        return productRepository.findByPriceBetween(minPrice, maxPrice);
//    }

    public Page<Product> searchProducts(
            String name,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        return productRepository.searchProducts(
                name,
                minPrice,
                maxPrice,
                pageable
        );
    }

}
