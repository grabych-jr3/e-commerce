package com.ogidazepam.e_commerce.service;

import com.ogidazepam.e_commerce.dto.CustomerRegisterDTO;
import com.ogidazepam.e_commerce.enums.UserRole;
import com.ogidazepam.e_commerce.model.Cart;
import com.ogidazepam.e_commerce.model.Customer;
import com.ogidazepam.e_commerce.repository.CartRepository;
import com.ogidazepam.e_commerce.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerService customerService;

    @Captor
    private ArgumentCaptor<Customer> customerArgumentCaptor;

    @Captor
    private ArgumentCaptor<Cart> cartArgumentCaptor;

    @Test
    public void shouldSaveCustomer(){
        // Arrange
        CustomerRegisterDTO dto = new CustomerRegisterDTO();
        dto.setEmail("test@gmail.com");
        dto.setPassword("1234");
        dto.setFirstName("fName");
        dto.setLastName("lName");

        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded");

        // Act
        customerService.saveCustomer(dto);

        // Assert
        verify(customerRepository).save(customerArgumentCaptor.capture());

        Customer customer = customerArgumentCaptor.getValue();
        assertEquals(dto.getEmail(), customer.getEmail());
        assertEquals("encoded", customer.getPassword());
        assertEquals(dto.getFirstName(), customer.getFirstName());
        assertEquals(dto.getLastName(), customer.getLastName());
        assertEquals(UserRole.USER, customer.getRole());

        verify(cartRepository).save(cartArgumentCaptor.capture());

        Cart cart = cartArgumentCaptor.getValue();
        assertEquals(customer, cart.getCustomer());
    }
}
