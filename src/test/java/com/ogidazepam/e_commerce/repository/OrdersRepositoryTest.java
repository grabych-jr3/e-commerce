package com.ogidazepam.e_commerce.repository;

import com.ogidazepam.e_commerce.model.Customer;
import com.ogidazepam.e_commerce.model.Orders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class OrdersRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    private Customer customer;
    private Orders orders1;
    private Orders orders2;

    @BeforeEach
    void setUp(){
         this.customer = Customer.builder()
                .email("d@gmail.com")
                .password("1234")
                .firstName("test")
                .lastName("test")
                .build();

         this.orders1 = Orders.builder()
                 .totalAmount(100.00)
                 .customer(customer)
                 .build();

        this.orders2 = Orders.builder()
                .totalAmount(100.00)
                .customer(customer)
                .build();
    }

    @Test
    void findOrderByIdAndCustomerId_shouldReturnOrder(){
        // Given
        customerRepository.save(customer);
        ordersRepository.save(orders1);

        long orderId = orders1.getId();
        long customerId = customer.getId();

        // When
        Orders ordersFound = ordersRepository.findByIdAndCustomerId(orderId, customerId).get();

        // Then
        assertThat(ordersFound).isNotNull();
        assertThat(ordersFound.getCustomer().getId()).isEqualTo(customer.getId());
        assertThat(ordersFound.getId()).isEqualTo(orders1.getId());
    }

    @Test
    void findAllOrdersByCustomerIdAndOrderByDate_shouldReturnListOfOrdersInDescOrder(){
        // Given
        customerRepository.save(customer);
        ordersRepository.save(orders1);
        ordersRepository.save(orders2);

        long customerId = customer.getId();

        // When
        List<Orders> ordersList = ordersRepository.findAllByCustomerIdOrderByOrderDateDesc(customerId);

        // Then
        assertThat(ordersList).isNotNull();
        assertThat(ordersList.size()).isEqualTo(2);
        assertThat(ordersList.get(0)).isEqualTo(orders2);
    }
}
