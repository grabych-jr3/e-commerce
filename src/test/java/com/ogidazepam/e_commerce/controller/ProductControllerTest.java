package com.ogidazepam.e_commerce.controller;

import com.ogidazepam.e_commerce.config.SecurityConfig;
import com.ogidazepam.e_commerce.dto.ProductViewDTO;
import com.ogidazepam.e_commerce.service.ProductService;
import com.ogidazepam.e_commerce.service.security.CustomUserDetailsService;
import com.ogidazepam.e_commerce.service.security.JwtService;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {ProductController.class, SecurityConfig.class})
public class ProductControllerTest {

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnAllProductDtos() throws Exception {
        ProductViewDTO product1 = new ProductViewDTO("name1", "description1", 10.0, 10);
        ProductViewDTO product2 = new ProductViewDTO("name2", "description2", 10.0, 10);
        List<ProductViewDTO> list = List.of(product1, product2);

        when(productService.findAllProducts()).thenReturn(list);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        verify(productService, times(1)).findAllProducts();
    }

    @Test
    void shouldReturnProductDtoById() throws Exception {
        ProductViewDTO product1 = new ProductViewDTO("name1", "description1", 10.0, 10);

        when(productService.findProduct(1L)).thenReturn(product1);

        mockMvc.perform(get("/api/v1/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("name1"))
                .andExpect(jsonPath("$.description").value("description1"))
                .andExpect(jsonPath("$.price").value(10.0))
                .andExpect(jsonPath("$.quantity").value(10));
        verify(productService, times(1)).findProduct(1L);
    }

    @Test
    void shouldReturnProductDtosByNameParam() throws Exception {
        ProductViewDTO product1 = new ProductViewDTO("phone1", "description1", 10.0, 10);
        ProductViewDTO product2 = new ProductViewDTO("phone2", "description2", 10.0, 10);

        when(productService.findProductsByName("phone"))
                .thenReturn(List.of(product1, product2));

        mockMvc.perform(get("/api/v1/products/search")
                .param("name", "phone"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(status().isOk());
        verify(productService, times(1)).findProductsByName("phone");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateProduct_whenUserIsAdmin() throws Exception {
        ProductViewDTO dto = new ProductViewDTO("name1", "description1", 10.0, 10);
        String dtoJson = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/api/v1/products")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(dtoJson))
                .andExpect(status().isCreated());
        verify(productService, times(1)).saveProduct(dto);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403_whenUserIsNotAdmin_forCreateProduct() throws Exception{
        ProductViewDTO dto = new ProductViewDTO("name1", "description1", 10.0, 10);
        String dtoJson = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/api/v1/products")
                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                .content(dtoJson))
                .andExpect(status().isForbidden());
        verify(productService, never()).saveProduct(dto);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateProduct() throws Exception {
        ProductViewDTO dto = new ProductViewDTO("name1", "description1", 10.0, 10);
        String dtoJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(patch("/api/v1/products/{id}", 1L)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(dtoJson))
                .andExpect(status().isOk());
        verify(productService, times(1)).updateProduct(1L, dto);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403_whenUserIsNotAdmin_forUpdateProduct() throws Exception{
        ProductViewDTO dto = new ProductViewDTO("name1", "description1", 10.0, 10);
        String dtoJson = objectMapper.writeValueAsString(dto);
        mockMvc.perform(patch("/api/v1/products/{id}", 1L)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(dtoJson))
                .andExpect(status().isForbidden());
        verify(productService, never()).updateProduct(1L, dto);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteProduct() throws Exception {
        long id = 1L;
        mockMvc.perform(delete("/api/v1/products/{id}", id))
                .andExpect(status().isNoContent());
        verify(productService, times(1)).deleteProduct(id);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldDeleteProduct_whenUserIsNotAdmin_forDeleteProduct() throws Exception {
        long id = 1L;
        mockMvc.perform(delete("/api/v1/products/{id}", id))
                .andExpect(status().isForbidden());
        verify(productService, never()).deleteProduct(id);
    }
}
