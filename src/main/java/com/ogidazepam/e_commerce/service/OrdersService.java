package com.ogidazepam.e_commerce.service;

import com.ogidazepam.e_commerce.dto.OrderViewDTO;
import com.ogidazepam.e_commerce.enums.OrderStatus;
import com.ogidazepam.e_commerce.model.*;
import com.ogidazepam.e_commerce.repository.CartRepository;
import com.ogidazepam.e_commerce.repository.OrdersRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrdersService {

    private final CartRepository cartRepository;
    private final OrdersRepository orderRepository;

    @Transactional
    public void createOrder(Customer customer) {
        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));

        Orders order = new Orders();

        order.setOrderItemList(
                cart.getCartItemList().stream().map(c -> new OrderItem(
                        c.getProduct().getName(),
                        c.getQuantity(),
                        c.getUnitPrice(),
                        order,
                        c.getProduct().getId()
                )).collect(Collectors.toList())
        );

        double total_amount = 0;

        for(OrderItem orderItem : order.getOrderItemList()){
            total_amount += orderItem.getUnitPrice() * orderItem.getQuantity();
        }

        order.setStatus(OrderStatus.PROCESSING);
        order.setTotalAmount(total_amount);
        order.setCustomer(customer);


        orderRepository.save(order);
    }

    public List<OrderViewDTO> findAllOrders(Customer customer) {
        List<Orders> orders = orderRepository.findAllByCustomerId(customer.getId());
        return orders.stream().map(o -> new OrderViewDTO(
                o.getStatus(),
                o.getTotalAmount()
        )).toList();
    }


    public OrderViewDTO findOrderById(long id, Customer customer) {
        Orders order = orderRepository.findByIdAndCustomerId(id, customer.getId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        return new OrderViewDTO(
                order.getStatus(),
                order.getTotalAmount()
        );
    }
}