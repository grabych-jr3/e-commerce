package com.ogidazepam.e_commerce.controller;

import com.ogidazepam.e_commerce.config.SecurityConfig;
import com.ogidazepam.e_commerce.dto.CartItemAddingDTO;
import com.ogidazepam.e_commerce.dto.CartItemViewDTO;
import com.ogidazepam.e_commerce.dto.ChangeQuantityDTO;
import com.ogidazepam.e_commerce.dto.RemoveCartItemDTO;
import com.ogidazepam.e_commerce.enums.UserRole;
import com.ogidazepam.e_commerce.model.Customer;
import com.ogidazepam.e_commerce.service.CartService;
import com.ogidazepam.e_commerce.service.security.CustomUserDetailsService;
import com.ogidazepam.e_commerce.service.security.JwtService;
import com.ogidazepam.e_commerce.util.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@Import(SecurityConfig.class)
public class CartControllerTest {

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

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
    void shouldAddItemToTheCart() throws Exception {
        CartItemAddingDTO dto = new CartItemAddingDTO(1L);

        mockMvc.perform(post("/api/v1/cart/add")
                        .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
        verify(cartService).addToCart(eq(dto), eq(customer));
    }

    @Test
    void shouldReturnAllCartItemDtosFromCart() throws Exception {
        CartItemViewDTO dto1 = new CartItemViewDTO(1L, 2, 20.0);
        CartItemViewDTO dto2 = new CartItemViewDTO(2L, 2, 20.0);

        when(cartService.getCart(customer)).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/v1/cart")
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(cartService, times(1)).getCart(customer);
    }

    @Test
    void shouldChangeQuantity() throws Exception{
        ChangeQuantityDTO dto = new ChangeQuantityDTO(1L, 2);

        mockMvc.perform(patch("/api/v1/cart/change-quantity")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
        verify(cartService, times(1)).changeQuantity(dto, customer);
    }

    @Test
    void shouldRemoveItemFromCart() throws Exception {
        RemoveCartItemDTO dto = new RemoveCartItemDTO(1L);

        mockMvc.perform(delete("/api/v1/cart/delete-item")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
        verify(cartService, times(1)).removeItemFromCart(dto, customer);
    }

    @Test
    void shouldClearTheCart() throws Exception {
        mockMvc.perform(delete("/api/v1/cart/clear-cart")
                .with(user(userDetails)))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).clearTheCart(customer);
    }
}
