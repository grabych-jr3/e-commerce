package com.ogidazepam.e_commerce.repository;

import com.ogidazepam.e_commerce.model.Cart;
import com.ogidazepam.e_commerce.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Cart cart;
    private Customer customer;

    @BeforeEach
    void setUp(){
        this.customer = Customer.builder()
                .email("d@gmail.com")
                .password("1234")
                .firstName("test")
                .lastName("test")
                .build();

        this.cart = Cart.builder().customer(customer).build();
    }

    @Test
    void findCartByCustomerId_shouldReturnCart(){
        // Given
        customerRepository.save(customer);
        cartRepository.save(cart);

        long customerId = customer.getId();

        // When
        Cart cartFound = cartRepository.findByCustomerId(customerId).get();

        // Then
        assertThat(cartFound).isNotNull();
        assertThat(cartFound.getCustomer().getId()).isEqualTo(customer.getId());
    }
}
