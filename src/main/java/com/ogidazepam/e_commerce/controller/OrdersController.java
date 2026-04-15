package com.ogidazepam.e_commerce.controller;

import com.ogidazepam.e_commerce.dto.OrderViewDTO;
import com.ogidazepam.e_commerce.service.OrdersService;
import com.ogidazepam.e_commerce.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<OrderViewDTO>> findAllOrders(@AuthenticationPrincipal CustomUserDetails userDetails){
        List<OrderViewDTO> dtos = orderService.findAllOrders(userDetails.customer());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderViewDTO> findOrderById(@PathVariable long id,
                                                      @AuthenticationPrincipal CustomUserDetails userDetails){
        OrderViewDTO dto = orderService.findOrderById(id, userDetails.customer());
        return ResponseEntity.ok(dto);
    }
}
