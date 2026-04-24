package com.ogidazepam.e_commerce.service;

import com.ogidazepam.e_commerce.dto.OrderViewDTO;
import com.ogidazepam.e_commerce.enums.OrderStatus;
import com.ogidazepam.e_commerce.exceptions.CartIsEmptyException;
import com.ogidazepam.e_commerce.exceptions.ResourceNotFoundException;
import com.ogidazepam.e_commerce.model.*;
import com.ogidazepam.e_commerce.repository.CartRepository;
import com.ogidazepam.e_commerce.repository.OrdersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdersServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrdersService ordersService;

    @Captor
    private ArgumentCaptor<Orders> orderArgumentCaptor;

    @Test
    public void shouldCreateOrder(){
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);

        Product product1 = new Product("name1", "description1", 20.0, 10);
        product1.setId(1L);
        Product product2 = new Product("name2", "description2", 5, 10);
        product2.setId(2L);

        Cart cart = new Cart(customer);

        CartItem item1 = new CartItem(4, product1.getPrice(), cart, product1);
        CartItem item2 = new CartItem(2, product2.getPrice(), cart, product2);

        cart.setCartItemList(List.of(item1, item2));

        when(cartRepository.findByCustomerId(customer.getId()))
                .thenReturn(Optional.of(cart));

        // Act
        ordersService.createOrder(customer);

        // Assert
        verify(ordersRepository).save(orderArgumentCaptor.capture());

        Orders order = orderArgumentCaptor.getValue();
        double total_amount = (item1.getUnitPrice() * item1.getQuantity()) +
                (item2.getUnitPrice() * item2.getQuantity());

        assertEquals(OrderStatus.PROCESSING, order.getStatus());
        assertEquals(total_amount, order.getTotalAmount());
        assertEquals(customer, order.getCustomer());

        OrderItem o1 = order.getOrderItemList().get(0);
        assertEquals(item1.getQuantity(), o1.getQuantity());
        assertEquals(item1.getUnitPrice(), o1.getUnitPrice());
        assertEquals(item1.getProduct().getId(), o1.getProductId());

        OrderItem o2 = order.getOrderItemList().get(1);
        assertEquals(item2.getQuantity(), o2.getQuantity());
        assertEquals(item2.getUnitPrice(), o2.getUnitPrice());
        assertEquals(item2.getProduct().getId(), o2.getProductId());

        verify(cartService).clearTheCart(order.getCustomer());
    }

    @Test
    public void shouldThrowException_whenCartNotFound_forCreateOrder(){
        Customer customer = new Customer();
        customer.setId(1L);

        when(cartRepository.findByCustomerId(customer.getId()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ordersService.createOrder(customer));
        verify(ordersRepository, never()).save(any(Orders.class));
        verify(cartService, never()).clearTheCart(any(Customer.class));
    }

    @Test
    public void shouldThrowException_whenCartIsEmpty_forCreateOrder(){
        Customer customer = new Customer();
        customer.setId(1L);

        Cart cart = new Cart(customer);
        cart.setCartItemList(List.of());

        when(cartRepository.findByCustomerId(customer.getId()))
                .thenReturn(Optional.of(cart));

        assertThrows(CartIsEmptyException.class, () -> ordersService.createOrder(customer));
        verify(ordersRepository, never()).save(any(Orders.class));
        verify(cartService, never()).clearTheCart(any(Customer.class));
    }

    @Test
    public void shouldReturnMappedDtos(){
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);

        Orders order1 = new Orders(OrderStatus.COMPLETED, 120.0, customer);
        order1.setId(1L);
        Orders order2 = new Orders(OrderStatus.PROCESSING, 1000.0, customer);
        order2.setId(2L);

        List<Orders> orders = List.of(order1, order2);

        when(ordersRepository.findAllByCustomerIdOrderByOrderDateDesc(customer.getId()))
                .thenReturn(orders);

        // Act
        List<OrderViewDTO> orderViewDTOS = ordersService.findAllOrders(customer);

        // Assert
        assertEquals(order1.getId(), orderViewDTOS.get(0).orderId());
        assertEquals(order1.getStatus(), orderViewDTOS.get(0).status());
        assertEquals(order1.getTotalAmount(), orderViewDTOS.get(0).totalAmount());

        assertEquals(order2.getId(), orderViewDTOS.get(1).orderId());
        assertEquals(order2.getStatus(), orderViewDTOS.get(1).status());
        assertEquals(order2.getTotalAmount(), orderViewDTOS.get(1).totalAmount());
    }

    @Test
    public void shouldReturnEmptyOrdersList(){
        Customer customer = new Customer();
        customer.setId(1L);

        List<Orders> orders = List.of();

        when(ordersRepository.findAllByCustomerIdOrderByOrderDateDesc(customer.getId()))
                .thenReturn(orders);

        List<OrderViewDTO> dtos = ordersService.findAllOrders(customer);

        assertEquals(0, dtos.size());
    }

    @Test
    public void shouldReturnMappedOrderDto(){
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);

        Orders order = new Orders();
        order.setId(1L);
        order.setStatus(OrderStatus.PROCESSING);
        order.setTotalAmount(100.0);

        when(ordersRepository.findByIdAndCustomerId(order.getId(), customer.getId()))
                .thenReturn(Optional.of(order));

        // Act
        OrderViewDTO dto = ordersService.findOrderById(order.getId(), customer);

        // Assert
        assertEquals(order.getId(), dto.orderId());
        assertEquals(order.getStatus(), dto.status());
        assertEquals(order.getTotalAmount(), dto.totalAmount());
    }

    @Test
    public void shouldThrowException_whenOrderNotFound_forFindOrderById(){
        Customer customer = new Customer();
        customer.setId(1L);

        long orderId = 1L;

        when(ordersRepository.findByIdAndCustomerId(orderId, customer.getId()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ordersService.findOrderById(orderId, customer));
    }
}
