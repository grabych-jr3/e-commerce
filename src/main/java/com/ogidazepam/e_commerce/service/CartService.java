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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public List<CartItemViewDTO> getCart(Customer customer){
        Cart cart =  cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        return cart.getCartItemList().stream().map(c -> new CartItemViewDTO(
                c.getId(),
                c.getQuantity(),
                c.getUnitPrice()
        )).toList();
    }

    @Transactional
    public void addToCart(CartItemAddingDTO dto, Customer customer) {
        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        Product product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndProduct(cart, product);

        int currentQuantity = existingCartItem.map(CartItem::getQuantity).orElse(0);
        if (product.getQuantity() <= currentQuantity){
            throw new ProductOutOfStockException("No items left");
        }

        if (existingCartItem.isPresent()){
            existingCartItem.get().setQuantity(existingCartItem.get().getQuantity() + 1);
        }else {
            CartItem cartItem = new CartItem(
                    1,
                    product.getPrice(),
                    cart,
                    product
            );
            cartItemRepository.save(cartItem);
        }
    }

    @Transactional
    public void changeQuantity(ChangeQuantityDTO dto, Customer customer) {
        CartItem cartItem = cartItemRepository.findByIdAndCartCustomerId(dto.cartItemId(), customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        Product product = cartItem.getProduct();

        if (product.getQuantity() < dto.quantity()){
            throw new ProductOutOfStockException("You can't order more than the store has");
        }

        cartItem.setQuantity(dto.quantity());
    }

    @Transactional
    public void removeItemFromCart(RemoveCartItemDTO dto, Customer customer){
        CartItem cartItem = cartItemRepository.findByIdAndCartCustomerId(dto.cartItemId(), customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearTheCart(Customer customer){
        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cartItemRepository.deleteAll(cart.getCartItemList());
    }
}
