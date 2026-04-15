package com.ogidazepam.e_commerce.controller;

import com.ogidazepam.e_commerce.dto.CartItemAddingDTO;
import com.ogidazepam.e_commerce.dto.CartItemViewDTO;
import com.ogidazepam.e_commerce.dto.ChangeQuantityDTO;
import com.ogidazepam.e_commerce.dto.RemoveCartItemDTO;
import com.ogidazepam.e_commerce.service.CartService;
import com.ogidazepam.e_commerce.util.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<Void> addToCart(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @RequestBody CartItemAddingDTO dto){
        cartService.addToCart(dto, userDetails.customer());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<CartItemViewDTO>> getCart(@AuthenticationPrincipal CustomUserDetails userDetails){
        List<CartItemViewDTO> dtos = cartService.getCart(userDetails.customer());
        return ResponseEntity.ok(dtos);
    }

    @PatchMapping("/change-quantity")
    public ResponseEntity<Void> changeQuantity(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @Valid @RequestBody ChangeQuantityDTO dto){
        cartService.changeQuantity(dto, userDetails.customer());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete-item")
    public ResponseEntity<Void> removeItemFromCart(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @Valid @RequestBody RemoveCartItemDTO dto){
        cartService.removeItemFromCart(dto,  userDetails.customer());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear-cart")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal CustomUserDetails userDetails){
        cartService.clearTheCart(userDetails.customer());
        return ResponseEntity.noContent().build();
    }
}
