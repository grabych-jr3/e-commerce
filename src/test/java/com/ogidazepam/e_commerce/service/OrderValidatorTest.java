package com.ogidazepam.e_commerce.service;

import com.ogidazepam.e_commerce.enums.OrderStatus;
import com.ogidazepam.e_commerce.exceptions.OrderAlreadyPaidException;
import com.ogidazepam.e_commerce.exceptions.ProductOutOfStockException;
import com.ogidazepam.e_commerce.model.OrderItem;
import com.ogidazepam.e_commerce.model.Orders;
import com.ogidazepam.e_commerce.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderValidatorTest {

    private OrderValidator orderValidator = new OrderValidator();

    @Test
    void shouldThrowException_whenOrderCompleted(){
        Orders order = Orders.builder()
                .id(1L)
                .status(OrderStatus.COMPLETED)
                .build();

        assertThrows(OrderAlreadyPaidException.class, () -> orderValidator.validateNotCompleted(order));
    }

    @Test
    void shouldThrowException_whenNotEnoughProductQuantity(){
        OrderItem item1 = OrderItem.builder()
                .id(1L)
                .quantity(5)
                .productId(1L)
                .build();

        Product product = Product.builder()
                .id(1L)
                .quantity(4)
                .build();

        assertThrows(ProductOutOfStockException.class, () ->
                orderValidator.validateStock(List.of(item1), Map.of(1L, product)));
    }
}
