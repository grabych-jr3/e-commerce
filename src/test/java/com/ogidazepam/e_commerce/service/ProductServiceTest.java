package com.ogidazepam.e_commerce.service;

import com.ogidazepam.e_commerce.dto.ProductViewDTO;
import com.ogidazepam.e_commerce.exceptions.ResourceNotFoundException;
import com.ogidazepam.e_commerce.model.Product;
import com.ogidazepam.e_commerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Captor
    ArgumentCaptor<Product> productCaptor;

    @Test
    public void shouldReturnProductViewDTO_whenProductExists(){
        Product product = Product.builder().id(1L).name("name").description("description").price(20.0).quantity(10).build();

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        ProductViewDTO productViewDTO = productService.findProduct(product.getId());

        assertEquals(product.getName(), productViewDTO.name());
        assertEquals(product.getDescription(), productViewDTO.description());
        assertEquals(product.getPrice(), productViewDTO.price());
        assertEquals(product.getQuantity(), productViewDTO.quantity());

        verify(productRepository).findById(product.getId());
    }

    @Test
    public void shouldThrowException_whenProductNotFound(){
        long id = 1L;

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.findProduct(id));
    }

    @Test
    public void shouldReturnMappedDtos_whenProductsFoundByName(){
        String name = "name";

        Product p1 = Product.builder().id(1L).name("name1").description("description1").price(20.0).quantity(10).build();
        Product p2 = Product.builder().id(2L).name("name2").description("description2").price(20.0).quantity(10).build();

        when(productRepository.findAllByNameContainingIgnoreCase(name)).thenReturn(List.of(p1, p2));

        // Act
        List<ProductViewDTO> dtos = productService.findProductsByName(name);

        // Assert
        assertEquals(2, dtos.size());
        assertEquals(p1.getName(), dtos.getFirst().name());
        assertEquals(p2.getName(), dtos.get(1).name());

        verify(productRepository).findAllByNameContainingIgnoreCase(name);
    }

    @Test
    public void shouldSaveProduct(){
        // Arrange
        ProductViewDTO dto = new ProductViewDTO(
                "name",
                "description",
                10.0,
                10
        );

        // Act
        productService.saveProduct(dto);

        // Assert
        verify(productRepository).save(productCaptor.capture());

        Product product = productCaptor.getValue();
        assertEquals("name", product.getName());
        assertEquals("description", product.getDescription());
        assertEquals(10.0, product.getPrice());
        assertEquals(10, product.getQuantity());
    }

    @Test
    public void shouldUpdateProduct_whenProductExists(){
        // Arrange
        long id = 1L;
        Product product = new Product(
                "oldName",
                "oldDescription",
                10.0,
                10
        );

        ProductViewDTO dto = new ProductViewDTO(
                "newName",
                "newDescription",
                20.0,
                5
        );

        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        // Act
        productService.updateProduct(id, dto);

        // Assert
        assertEquals("newName", product.getName());
        assertEquals("newDescription", product.getDescription());
        assertEquals(20.0, product.getPrice());
        assertEquals(5, product.getQuantity());

        verify(productRepository).findById(id);
    }

    @Test
    public void shouldThrowException_whenProductNotFound_forUpdateProduct(){
        long id = 1L;
        ProductViewDTO dto = new ProductViewDTO(
                "newName",
                "newDescription",
                20.0,
                5
        );

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(id, dto));
    }

    @Test
    public void shouldDeleteProduct(){
        // Arrange
        long id = 1L;

        Product product = new Product(
                "name",
                "description",
                20.0,
                10
        );

        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        // Act
        productService.deleteProduct(id);

        // Assert
        verify(productRepository).findById(id);
        verify(productRepository).delete(product);
    }

    @Test
    public void shouldThrowException_whenProductNotFound_forDeleteProduct(){
        long id = 1L;

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(id));
        verify(productRepository, never()).delete(any());
    }
}
