package com.ogidazepam.e_commerce.service.security;

import com.ogidazepam.e_commerce.model.Customer;
import com.ogidazepam.e_commerce.util.CustomUserDetails;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JwtServiceTest {

    private JwtService jwtService = new JwtService("seanawngoNVniwENFENFNwWEOIFNIANODKFAEIFNW3438NF29nfowWNFOwn3foiNGSLZNGLFDKFJNGZKJNGZRNGNsjrg");

    @Test
    void shouldReturnToken(){
        CustomUserDetails userDetails = new CustomUserDetails(Customer.builder().email("email").build());

        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals("email", username);
    }
}
