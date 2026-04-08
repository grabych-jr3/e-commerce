package com.ogidazepam.e_commerce.service;

import com.ogidazepam.e_commerce.dto.CustomerRegisterDTO;
import com.ogidazepam.e_commerce.enums.UserRole;
import com.ogidazepam.e_commerce.model.Customer;
import com.ogidazepam.e_commerce.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomerService {

    private final PasswordEncoder passwordEncoder;
    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(PasswordEncoder passwordEncoder, CustomerRepository customerRepository) {
        this.passwordEncoder = passwordEncoder;
        this.customerRepository = customerRepository;
    }

    public void saveCustomer(CustomerRegisterDTO dto) {
        Customer customer = new Customer();

        customer.setEmail(dto.getEmail());
        customer.setPassword(passwordEncoder.encode(dto.getPassword()));
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());

        customer.setRole(UserRole.USER);

        customerRepository.save(customer);
    }
}
