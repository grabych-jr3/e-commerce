package com.ogidazepam.e_commerce.repository;

import com.ogidazepam.e_commerce.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void findCustomerByEmail_shouldReturnCustomer(){
        // Given
        Customer customer = Customer.builder()
                .email("d@gmail.com")
                .password("1234")
                .firstName("test")
                .lastName("test")
                .build();

        customerRepository.save(customer);

        String email = "d@gmail.com";
        // When
        Customer customerFound = customerRepository.findByEmail(email).get();

        // Then
        assertThat(customerFound).isNotNull();
        assertThat(customerFound.getEmail()).isEqualTo(customer.getEmail());
    }
}
