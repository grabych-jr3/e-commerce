package com.ogidazepam.e_commerce.service.security;

import com.ogidazepam.e_commerce.model.Customer;
import com.ogidazepam.e_commerce.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void shouldReturnCustomUserDetails(){
        String username = "username@gmail.com";
        Customer customer = Customer.builder().id(1L).build();

        when(customerRepository.findByEmail(username))
                .thenReturn(Optional.of(customer));

        com.ogidazepam.e_commerce.util.CustomUserDetails userDetails = userDetailsService.loadUserByUsername(username);

        assertEquals(customer, userDetails.customer());
    }

    @Test
    void shouldThrowException_whenCustomerNotFound(){
        String username = "username@gmail.com";
        when(customerRepository.findByEmail(username))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(username));
    }
}
