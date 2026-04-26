package com.ogidazepam.e_commerce.service;

import com.ogidazepam.e_commerce.dto.OrderByIdDTO;
import com.ogidazepam.e_commerce.enums.OrderStatus;
import com.ogidazepam.e_commerce.enums.PaymentStatus;
import com.ogidazepam.e_commerce.exceptions.OrderAlreadyPaidException;
import com.ogidazepam.e_commerce.exceptions.ProductOutOfStockException;
import com.ogidazepam.e_commerce.exceptions.ResourceNotFoundException;
import com.ogidazepam.e_commerce.model.*;
import com.ogidazepam.e_commerce.repository.OrdersRepository;
import com.ogidazepam.e_commerce.repository.PaymentRepository;
import com.ogidazepam.e_commerce.repository.ProductRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderValidator orderValidator;

    @Mock
    private StripeService stripeService;

    @InjectMocks
    private PaymentService paymentService;

    @Captor
    private ArgumentCaptor<Payment> paymentArgumentCaptor;

    @Test
    void shouldReturnCheckoutPage() throws StripeException {
        // Arrange
        Customer customer = Customer.builder().id(1L).build();

        OrderByIdDTO dto = new OrderByIdDTO(1L);

        Orders order = Orders.builder()
                .status(OrderStatus.PROCESSING)
                .id(1L)
                .build();

        Product product = Product.builder()
                .id(1L)
                .quantity(10)
                .build();

        OrderItem item1 = OrderItem.builder()
                .quantity(2)
                .productId(product.getId())
                .order(order)
                .build();

        order.setOrderItemList(List.of(item1));

        when(ordersRepository.findByIdAndCustomerId(dto.orderId(), customer.getId()))
                .thenReturn(Optional.of(order));
        when(productRepository.findAllById(anyList()))
                .thenReturn(List.of(product));

        Session session = mock(Session.class);
        when(session.getId()).thenReturn("sess_123");
        when(session.getUrl()).thenReturn("http://stripe.url");
        when(session.getAmountTotal()).thenReturn(1000L);
        when(session.getCurrency()).thenReturn("usd");

        when(stripeService.createSession(order)).thenReturn(session);

        // Act
        String result = paymentService.checkout(customer, dto);

        // Assert
        assertEquals("http://stripe.url", result);

        verify(orderValidator).validateNotCompleted(order);
        verify(orderValidator).validateStock(any(), any());
        verify(stripeService).createSession(order);
        verify(paymentRepository).save(paymentArgumentCaptor.capture());

        Payment payment = paymentArgumentCaptor.getValue();
        assertEquals("sess_123", payment.getStripeSessionId());
        assertEquals(PaymentStatus.CREATED, payment.getStatus());
        assertEquals(1000L, payment.getAmount());
        assertEquals("usd", payment.getCurrency());
        assertEquals(order, payment.getOrder());
    }

    @Test
    void shouldThrowException_whenOrderNotFound_forCheckout() throws StripeException {
        Customer customer = Customer.builder().id(1L).build();
        OrderByIdDTO dto = new OrderByIdDTO(1L);

        when(ordersRepository.findByIdAndCustomerId(dto.orderId(), customer.getId()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.checkout(customer, dto));
        verify(orderValidator, never()).validateNotCompleted(any());
        verify(productRepository, never()).findAllById(List.of(1L));
        verify(stripeService, never()).createSession(any());
        verify(orderValidator, never()).validateStock(any(), any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_whenOrderHasBeenAlreadyPaid() throws StripeException {
        Customer customer = Customer.builder().id(1L).build();
        OrderByIdDTO dto = new OrderByIdDTO(1L);

        Orders order = Orders.builder()
                .id(1L)
                .status(OrderStatus.PROCESSING)
                .build();

        when(ordersRepository.findByIdAndCustomerId(dto.orderId(), customer.getId()))
                .thenReturn(Optional.of(order));
        doThrow(OrderAlreadyPaidException.class).when(orderValidator).validateNotCompleted(order);
        assertThrows(OrderAlreadyPaidException.class,
                () -> paymentService.checkout(customer, dto));
        verify(productRepository, never()).findAllById(List.of(1L));
        verify(stripeService, never()).createSession(order);
        verify(orderValidator, never()).validateStock(any(), any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_whenProductOutOfStock(){
        Customer customer = Customer.builder().id(1L).build();
        OrderByIdDTO dto = new OrderByIdDTO(1L);

        Orders order = Orders.builder()
                .id(1L)
                .status(OrderStatus.PROCESSING)
                .orderItemList(List.of(
                        OrderItem.builder().id(1L).quantity(2).build()
                ))
                .build();

        when(ordersRepository.findByIdAndCustomerId(dto.orderId(), customer.getId()))
                .thenReturn(Optional.of(order));

        when(productRepository.findAllById(any()))
                .thenReturn(List.of(Product.builder().id(1L).quantity(0).build()));

        doThrow(ProductOutOfStockException.class)
                .when(orderValidator)
                .validateStock(anyList(), anyMap());

        assertThrows(ProductOutOfStockException.class,
                () -> paymentService.checkout(customer, dto));

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldChangeStatusesAndReduceProductsQuantities(){
        // Arrange
        String sessionId = "sessionId";

        Orders order = Orders.builder()
                .status(OrderStatus.PROCESSING)
                .id(1L)
                .build();

        Product product = Product.builder()
                .id(1L)
                .quantity(10)
                .price(20.0)
                .build();

        OrderItem item1 = OrderItem.builder()
                .quantity(2)
                .productId(product.getId())
                .unitPrice(product.getPrice())
                .order(order)
                .build();

        order.setOrderItemList(List.of(item1));

        Payment payment = Payment.builder()
                .stripeSessionId(sessionId)
                .id(1L)
                .status(PaymentStatus.CREATED)
                .order(order)
                .build();

        when(paymentRepository.findByStripeSessionId(sessionId))
                .thenReturn(Optional.of(payment));

        when(productRepository.findAllById(List.of(product.getId())))
                .thenReturn(List.of(product));

        // Act
        paymentService.handleSucceededEvent(sessionId);

        // Assert
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());

        assertEquals(8, product.getQuantity());

        verify(paymentRepository, times(1)).findByStripeSessionId(sessionId);
        verify(ordersRepository, times(1)).save(order);
        verify(productRepository, times(1)).findAllById(List.of(product.getId()));
    }

    @Test
    void shouldThrowException_whenPaymentNotFound_forHandleSucceededEvent(){
        when(paymentRepository.findByStripeSessionId("id"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.handleSucceededEvent("id"));
        verify(productRepository, never()).findAllById(any());
        verify(ordersRepository, never()).save(any());
    }

    @Test
    void shouldChangeStatus_whenPaymentFailed(){
        Payment payment = Payment.builder()
                .status(PaymentStatus.CREATED)
                .build();

        when(paymentRepository.findFirstByOrderIdAndStatusOrderByCreatedAtDesc(1L, PaymentStatus.CREATED))
                .thenReturn(Optional.of(payment));

        paymentService.handleFailedEvent(1L);

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
    }

    @Test
    void shouldThrowException_whenPaymentNotFound_forHandleFailedEvent(){
        when(paymentRepository.findFirstByOrderIdAndStatusOrderByCreatedAtDesc(1L, PaymentStatus.CREATED))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.handleFailedEvent(1L));
    }
}
