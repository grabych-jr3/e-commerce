package com.ogidazepam.e_commerce.controller;

import com.ogidazepam.e_commerce.dto.CartItemAddingDTO;
import com.ogidazepam.e_commerce.dto.CartItemViewDTO;
import com.ogidazepam.e_commerce.model.Cart;
import com.ogidazepam.e_commerce.service.CartService;
import com.ogidazepam.e_commerce.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
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
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<CartItemViewDTO>> getCart(@AuthenticationPrincipal CustomUserDetails userDetails){
        List<CartItemViewDTO> dtos = cartService.getCart(userDetails.customer());
        return ResponseEntity.ok(dtos);
    }
}
