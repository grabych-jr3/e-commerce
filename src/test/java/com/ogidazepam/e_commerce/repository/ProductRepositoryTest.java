package com.ogidazepam.e_commerce.repository;

import com.ogidazepam.e_commerce.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp(){
        product1 = Product.builder()
                .name("Iphone 16 pro")
                .description("desc1")
                .price(10)
                .quantity(10)
                .build();

        product2 = Product.builder()
                .name("iPHOne 17")
                .description("desc2")
                .price(10)
                .quantity(10)
                .build();
    }

    @Test
    void findAllByNameContainingIgnoreCase_shouldReturnProductsWhenNameMatches(){
        // Given
        productRepository.save(product1);
        productRepository.save(product2);

        // When
        List<Product> products = productRepository.findAllByNameContainingIgnoreCase("iphone");

        // Then
        assertThat(products).isNotNull();
        assertThat(products.size()).isEqualTo(2);
    }

    @Test
    void findAllByNameContainingIgnoreCase_shouldReturnEmptyListWhenNoMatch(){
        // Given
        productRepository.save(product1);
        productRepository.save(product2);

        // When
        List<Product> products = productRepository.findAllByNameContainingIgnoreCase("none");

        // Then
        assertThat(products).isNotNull();
        assertThat(products.size()).isEqualTo(0);
    }
}
