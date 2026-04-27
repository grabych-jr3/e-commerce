package com.ogidazepam.e_commerce.controller;

import com.ogidazepam.e_commerce.config.SecurityConfig;
import com.ogidazepam.e_commerce.dto.CustomerLoginDTO;
import com.ogidazepam.e_commerce.dto.CustomerRegisterDTO;
import com.ogidazepam.e_commerce.enums.UserRole;
import com.ogidazepam.e_commerce.model.Customer;
import com.ogidazepam.e_commerce.service.CustomerService;
import com.ogidazepam.e_commerce.service.security.CustomUserDetailsService;
import com.ogidazepam.e_commerce.service.security.JwtService;
import com.ogidazepam.e_commerce.util.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTest {

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateCustomer() throws Exception{
        CustomerRegisterDTO dto = new CustomerRegisterDTO("email@gmail.com", "1234", "fName1", "lName");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(customerService, times(1)).saveCustomer(dto);
    }

    @Test
    void shouldReturnJWT() throws Exception{
        CustomerLoginDTO dto = new CustomerLoginDTO("email@gmail.com", "1234");
        Customer customer = Customer.builder().id(1L).role(UserRole.USER).build();
        CustomUserDetails userDetails = new CustomUserDetails(customer);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        String jwt = "jwt-token";
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(jwtService.generateToken((CustomUserDetails) authentication.getPrincipal())).thenReturn(jwt);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(jwt));
        verify(authenticationManager, times(1))
                .authenticate(any(Authentication.class));
        verify(jwtService, times(1))
                .generateToken((CustomUserDetails) authentication.getPrincipal());
    }
}
