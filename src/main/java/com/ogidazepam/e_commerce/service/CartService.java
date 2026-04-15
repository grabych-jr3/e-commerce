package com.ogidazepam.e_commerce.service;

import com.ogidazepam.e_commerce.dto.CartItemAddingDTO;
import com.ogidazepam.e_commerce.dto.CartItemViewDTO;
import com.ogidazepam.e_commerce.dto.ChangeQuantityDTO;
import com.ogidazepam.e_commerce.dto.RemoveCartItemDTO;
import com.ogidazepam.e_commerce.model.Cart;
import com.ogidazepam.e_commerce.model.CartItem;
import com.ogidazepam.e_commerce.model.Customer;
import com.ogidazepam.e_commerce.model.Product;
import com.ogidazepam.e_commerce.repository.CartItemRepository;
import com.ogidazepam.e_commerce.repository.CartRepository;
import com.ogidazepam.e_commerce.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    /* TODO
        1. Правильно обробити exceptions
     */
    public void addToCart(CartItemAddingDTO dto, Customer customer) {
        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Cart not found"));

        Product product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (product.getQuantity() == 0){
            throw new RuntimeException("No items left");
        }

        CartItem cartItem = new CartItem(
                1,
                product.getPrice(),
                cart,
                product
        );

        cartItemRepository.save(cartItem);
    }

    public List<CartItemViewDTO> getCart(Customer customer){
        Cart cart =  cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Cart not found"));

        return cart.getCartItemList().stream().map(c -> new CartItemViewDTO(
                c.getId(),
                c.getQuantity(),
                c.getPrice()
        )).toList();
    }

    public void changeQuantity(ChangeQuantityDTO dto, Customer customer) {
        CartItem cartItem = cartItemRepository.findByIdAndCartCustomerId(dto.cartItemId(), customer.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found"));

        Product product = cartItem.getProduct();

        if (product.getQuantity() < dto.quantity()){
            throw new RuntimeException("You can't order more than the store has");
        }

        cartItem.setQuantity(dto.quantity());
    }

    public void removeItemFromCart(RemoveCartItemDTO dto, Customer customer){
        CartItem cartItem = cartItemRepository.findByIdAndCartCustomerId(dto.cartItemId(), customer.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found"));

        cartItemRepository.delete(cartItem);
    }

    public void clearTheCart(Customer customer){
        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));

        cartItemRepository.deleteAll(cart.getCartItemList());
    }
}
