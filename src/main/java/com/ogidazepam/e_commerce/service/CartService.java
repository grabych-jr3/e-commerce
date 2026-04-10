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
import org.springframework.security.access.prepost.PreAuthorize;
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
        2. Якщо користувач ще раз відправить запит, то CartItem створиться заново,
        але кількість предметів у Product відніметься 2 рази
        3. Налаштувати безпеку для Update та Delete методів
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

    // ! Користувач може змінювати тільки свої предмети в кошику
    public void changeQuantity(ChangeQuantityDTO dto, Customer customer) {
        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));

        CartItem cartItem = cart.getCartItemList().stream()
                .filter(c -> c.getId() == dto.cartItemId())
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found"));

        Product product = productRepository.findById(cartItem.getProduct().getId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (product.getQuantity() < dto.quantity()){
            throw new RuntimeException("You can't order more than the store has");
        }

        cartItem.setQuantity(dto.quantity());
        cartItemRepository.save(cartItem);
    }

    // ! Користувач може видаляти тільки свої предмети в кошику
    public void removeItemFromCart(RemoveCartItemDTO dto, Customer customer){
        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));

        CartItem cartItem = cart.getCartItemList().stream()
                .filter(c -> c.getId() == dto.cartItemId())
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found"));

        cartItemRepository.delete(cartItem);
    }
}
