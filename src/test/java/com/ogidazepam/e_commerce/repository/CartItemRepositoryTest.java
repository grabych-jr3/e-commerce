package com.ogidazepam.e_commerce.repository;

import com.ogidazepam.e_commerce.model.Cart;
import com.ogidazepam.e_commerce.model.CartItem;
import com.ogidazepam.e_commerce.model.Customer;
import com.ogidazepam.e_commerce.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;
    private Product product;
    private Cart cart;

    private CartItem cartItem;

    @BeforeEach
    void setUp(){
        this.customer = Customer.builder()
                .email("d@gmail.com")
                .password("1234")
                .firstName("test")
                .lastName("test")
                .build();

        this.product = Product.builder()
                .name("product")
                .description("desc")
                .price(10.00)
                .quantity(10)
                .build();

        this.cart = Cart.builder()
                .customer(customer)
                .build();

        this.cartItem = CartItem.builder()
                .cart(this.cart)
                .product(this.product)
                .quantity(1)
                .unitPrice(10.0)
                .build();
    }

    @Test
    void findCartItemByIdAndCustomerId_shouldReturnCartItem(){
        // Given
        customerRepository.save(customer);
        cartRepository.save(cart);
        productRepository.save(product);
        cartItemRepository.save(cartItem);

        long cartItemId = cartItem.getId();
        long customerId = customer.getId();

        // When
        CartItem cartItemFound = cartItemRepository.findByIdAndCartCustomerId(cartItemId, customerId).get();

        // Then
        assertThat(cartItemFound).isNotNull();
        assertThat(cartItemFound.getId()).isEqualTo(cartItem.getId());
    }

    @Test
    void findCartItemByCartAndProduct_shouldReturnCartItem(){
        // Given
        customerRepository.save(customer);
        cartRepository.save(cart);
        productRepository.save(product);
        cartItemRepository.save(cartItem);

        // When
        CartItem cartItemFound = cartItemRepository.findByCartAndProduct(cart, product).get();

        // Then
        assertThat(cartItemFound).isNotNull();
        assertThat(cartItemFound.getCart().getId()).isEqualTo(cart.getId());
        assertThat(cartItemFound.getProduct().getId()).isEqualTo(product.getId());
    }
}
