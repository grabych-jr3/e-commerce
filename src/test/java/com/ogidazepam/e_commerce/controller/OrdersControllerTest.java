package com.ogidazepam.e_commerce.controller;

import com.ogidazepam.e_commerce.config.SecurityConfig;
import com.ogidazepam.e_commerce.dto.OrderViewDTO;
import com.ogidazepam.e_commerce.enums.OrderStatus;
import com.ogidazepam.e_commerce.enums.UserRole;
import com.ogidazepam.e_commerce.model.Customer;
import com.ogidazepam.e_commerce.service.OrdersService;
import com.ogidazepam.e_commerce.service.security.CustomUserDetailsService;
import com.ogidazepam.e_commerce.service.security.JwtService;
import com.ogidazepam.e_commerce.util.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdersController.class)
@Import(SecurityConfig.class)
public class OrdersControllerTest {

    @MockitoBean
    private OrdersService ordersService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Customer customer;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp(){
        customer = Customer.builder().id(1L).role(UserRole.USER).build();
        userDetails = new CustomUserDetails(customer);
    }

    @Test
    void shouldCreateOrder() throws Exception{
        mockMvc.perform(post("/api/v1/orders/create-order")
                .with(user(userDetails)))
                .andExpect(status().isCreated());

        verify(ordersService, times(1)).createOrder(customer);
    }

    @Test
    void shouldReturnOrderDtos() throws Exception{
        OrderViewDTO dto1 = new OrderViewDTO(1L, OrderStatus.PROCESSING, 120.0);
        OrderViewDTO dto2 = new OrderViewDTO(2L, OrderStatus.COMPLETED, 500.0);

        when(ordersService.findAllOrders(customer)).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/v1/orders")
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        verify(ordersService, times(1)).findAllOrders(customer);
    }

    @Test
    void shouldReturnOrderById() throws Exception{
        OrderViewDTO dto = new OrderViewDTO(1L, OrderStatus.PROCESSING, 120.0);

        when(ordersService.findOrderById(1L, customer))
                .thenReturn(dto);

        mockMvc.perform(get("/api/v1/orders/{id}", 1L)
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.status").value(OrderStatus.PROCESSING.name()))
                .andExpect(jsonPath("$.totalAmount").value(120.0));

        verify(ordersService, times(1)).findOrderById(1L, customer);
    }
}
