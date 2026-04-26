package com.ogidazepam.e_commerce.service;

import com.ogidazepam.e_commerce.enums.OrderStatus;
import com.ogidazepam.e_commerce.exceptions.OrderAlreadyPaidException;
import com.ogidazepam.e_commerce.exceptions.ProductOutOfStockException;
import com.ogidazepam.e_commerce.model.OrderItem;
import com.ogidazepam.e_commerce.model.Orders;
import com.ogidazepam.e_commerce.model.Product;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class OrderValidator {

    public void validateNotCompleted(Orders order){
        if(order.getStatus().equals(OrderStatus.COMPLETED)){
            throw new OrderAlreadyPaidException("Your order " + order.getId() + " is already paid");
        }
    }

    public void validateStock(List<OrderItem> items, Map<Long, Product> productMap){
        for (OrderItem orderItem : items){
            Product product = productMap.get(orderItem.getProductId());

            if (product.getQuantity() < orderItem.getQuantity()){
                throw new ProductOutOfStockException("Not enough product quantity in the store");
            }
        }
    }
}
