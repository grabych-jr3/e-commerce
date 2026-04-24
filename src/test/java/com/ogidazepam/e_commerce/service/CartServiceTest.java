package com.ogidazepam.e_commerce.service;

import com.ogidazepam.e_commerce.dto.CartItemAddingDTO;
import com.ogidazepam.e_commerce.dto.CartItemViewDTO;
import com.ogidazepam.e_commerce.dto.ChangeQuantityDTO;
import com.ogidazepam.e_commerce.dto.RemoveCartItemDTO;
import com.ogidazepam.e_commerce.exceptions.ProductOutOfStockException;
import com.ogidazepam.e_commerce.exceptions.ResourceNotFoundException;
import com.ogidazepam.e_commerce.model.Cart;
import com.ogidazepam.e_commerce.model.CartItem;
import com.ogidazepam.e_commerce.model.Customer;
import com.ogidazepam.e_commerce.model.Product;
import com.ogidazepam.e_commerce.repository.CartItemRepository;
import com.ogidazepam.e_commerce.repository.CartRepository;
import com.ogidazepam.e_commerce.repository.ProductRepository;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartService cartService;

    @Captor
    private ArgumentCaptor<CartItem> cartItemArgumentCaptor;

    @Test
    public void shouldReturnDtos_whenCartExists(){
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);

        CartItem cartItem1 = new CartItem();
        cartItem1.setId(1L);
        cartItem1.setUnitPrice(10.0);
        cartItem1.setQuantity(10);

        CartItem cartItem2 = new CartItem();
        cartItem2.setId(2L);
        cartItem2.setUnitPrice(20.0);
        cartItem2.setQuantity(20);

        Cart cart = new Cart();
        cart.setCartItemList(List.of(cartItem1, cartItem2));
        cart.setCustomer(customer);

        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(cart));

        // Act
        List<CartItemViewDTO> dtos = cartService.getCart(customer);

        // Assert
        assertEquals(2, dtos.size());

        assertEquals(cartItem1.getId(), dtos.get(0).cartItemId());
        assertEquals(cartItem1.getUnitPrice(), dtos.get(0).price());
        assertEquals(cartItem1.getQuantity(), dtos.get(0).quantity());

        assertEquals(cartItem2.getId(), dtos.get(1).cartItemId());
        assertEquals(cartItem2.getUnitPrice(), dtos.get(1).price());
        assertEquals(cartItem2.getQuantity(), dtos.get(1).quantity());

        verify(cartRepository).findByCustomerId(customer.getId());
    }

    @Test
    public void shouldThrowException_whenCartNotFound(){
        Customer customer = new Customer();
        customer.setId(1L);

        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.getCart(customer));
    }

    @Test
    public void shouldAddToCart_whenCartAndProductExist(){
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);

        CartItemAddingDTO cartItemAddingDTO = new CartItemAddingDTO(1L);

        Cart cart = new Cart(customer);

        Product product = new Product("name", "description", 10.0, 5);

        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(cartItemAddingDTO.productId())).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());

        // Act
        cartService.addToCart(cartItemAddingDTO, customer);

        // Assert
        verify(cartItemRepository).save(cartItemArgumentCaptor.capture());

        CartItem cartItem = cartItemArgumentCaptor.getValue();

        assertEquals(1, cartItem.getQuantity());
        assertEquals(product.getPrice(), cartItem.getUnitPrice());
        assertEquals(cart, cartItem.getCart());
        assertEquals(product, cartItem.getProduct());
    }

    @Test
    public void shouldChangeQuantity_whenCartItemAlreadyExists_forAddToCart(){
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);

        CartItemAddingDTO cartItemAddingDTO = new CartItemAddingDTO(1L);

        Cart cart = new Cart(customer);

        Product product = new Product("name", "description", 10.0, 5);

        CartItem cartItem = new CartItem(1, product.getPrice(), cart, product);

        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(cartItemAddingDTO.productId())).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product))
                .thenReturn(Optional.of(cartItem));

        // Act
        cartService.addToCart(cartItemAddingDTO, customer);

        // Assert
        assertEquals(2, cartItem.getQuantity());
        assertEquals(product.getPrice(), cartItem.getUnitPrice());
        assertEquals(cart, cartItem.getCart());
        assertEquals(product, cartItem.getProduct());

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    public void shouldThrowException_whenCartNotFound_forAddToCart(){
        Customer customer = new Customer();
        customer.setId(1L);

        CartItemAddingDTO cartItemAddingDTO = new CartItemAddingDTO(1L);

        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.addToCart(cartItemAddingDTO, customer));
    }

    @Test
    public void shouldThrowException_whenProductNotFound_forAddToCart(){
        Customer customer = new Customer();
        customer.setId(1L);

        CartItemAddingDTO cartItemAddingDTO = new CartItemAddingDTO(1L);

        Cart cart = new Cart(customer);

        when(cartRepository.findByCustomerId(customer.getId()))
                .thenReturn(Optional.of(cart));
        when(productRepository.findById(cartItemAddingDTO.productId()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.addToCart(cartItemAddingDTO, customer));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 1",
            "1, 2",
            "2, 2"
    })
    public void shouldThrowException_whenNoProductsLeft_forAddToCart(int pQty, int cIQty){
        Customer customer = new Customer();
        customer.setId(1L);

        CartItemAddingDTO cartItemAddingDTO = new CartItemAddingDTO(1L);

        Cart cart = new Cart(customer);

        Product product = new Product();
        product.setQuantity(pQty);

        CartItem cartItem = new CartItem();
        cartItem.setQuantity(cIQty);

        when(cartRepository.findByCustomerId(customer.getId()))
                .thenReturn(Optional.of(cart));
        when(productRepository.findById(cartItemAddingDTO.productId()))
                .thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product))
                .thenReturn(Optional.of(cartItem));

        assertThrows(ProductOutOfStockException.class, () -> cartService.addToCart(cartItemAddingDTO, customer));
    }

    @Test
    public void shouldChangeQuantity(){
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);

        ChangeQuantityDTO dto = new ChangeQuantityDTO(1L, 2);

        Product product = new Product();
        product.setQuantity(5);

        CartItem cartItem = new CartItem();
        cartItem.setQuantity(dto.quantity());
        cartItem.setProduct(product);

        when(cartItemRepository.findByIdAndCartCustomerId(dto.cartItemId(), customer.getId()))
                .thenReturn(Optional.of(cartItem));

        // Act
        cartService.changeQuantity(dto, customer);

        // Assert
        assertEquals(2, cartItem.getQuantity());
    }

    @Test
    public void shouldThrowException_whenCartItemNotFound_forChangeQuantity(){
        Customer customer = new Customer();
        customer.setId(1L);

        ChangeQuantityDTO dto = new ChangeQuantityDTO(1L, 2);

        when(cartItemRepository.findByIdAndCartCustomerId(dto.cartItemId(), customer.getId()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.changeQuantity(dto, customer));
    }

    @Test
    public void shouldThrowException_whenCartItemQtyIsHigherThanProductQty_forChangeQuantity(){
        Customer customer = new Customer();
        customer.setId(1L);

        ChangeQuantityDTO dto = new ChangeQuantityDTO(1L, 3);

        Product product = new Product();
        product.setQuantity(2);

        CartItem cartItem = new CartItem();
        cartItem.setQuantity(dto.quantity());
        cartItem.setProduct(product);

        when(cartItemRepository.findByIdAndCartCustomerId(dto.cartItemId(), customer.getId()))
                .thenReturn(Optional.of(cartItem));

        assertThrows(ProductOutOfStockException.class, () -> cartService.changeQuantity(dto, customer));
    }

    @Test
    public void shouldRemoveItemFromCart(){
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);

        RemoveCartItemDTO dto = new RemoveCartItemDTO(1L);

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);

        when(cartItemRepository.findByIdAndCartCustomerId(dto.cartItemId(), customer.getId()))
                .thenReturn(Optional.of(cartItem));

        // Act
        cartService.removeItemFromCart(dto, customer);

        // Assert
        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    public void shouldThrowException_whenCartItemNotFound_forRemoveItemFromCart(){
        Customer customer = new Customer();
        customer.setId(1L);

        RemoveCartItemDTO dto = new RemoveCartItemDTO(1L);

        when(cartItemRepository.findByIdAndCartCustomerId(dto.cartItemId(), customer.getId()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.removeItemFromCart(dto, customer));
        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }

    @Test
    public void shouldClearTheCart(){
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);

        Cart cart = new Cart(customer);

        when(cartRepository.findByCustomerId(customer.getId()))
                .thenReturn(Optional.of(cart));

        // Act
        cartService.clearTheCart(customer);

        // Assert
        verify(cartItemRepository).deleteAll(cart.getCartItemList());
    }

    @Test
    public void shouldThrowException_whenCartNotFound_forClearTheCart(){
        Customer customer = new Customer();
        customer.setId(1L);

        when(cartRepository.findByCustomerId(customer.getId()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.clearTheCart(customer));
        verify(cartItemRepository, never()).deleteAll(any(List.class));
    }
}
