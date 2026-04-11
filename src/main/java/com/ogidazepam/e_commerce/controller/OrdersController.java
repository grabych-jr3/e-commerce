package com.ogidazepam.e_commerce.controller;

import com.ogidazepam.e_commerce.service.OrdersService;
import com.ogidazepam.e_commerce.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService orderService;

    @PostMapping("/create-order")
    public ResponseEntity<Void> createOrder(@AuthenticationPrincipal CustomUserDetails userDetails){
        orderService.createOrder(userDetails.customer());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
