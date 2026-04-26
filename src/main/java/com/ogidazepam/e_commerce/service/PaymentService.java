package com.ogidazepam.e_commerce.service;

import com.ogidazepam.e_commerce.dto.OrderByIdDTO;
import com.ogidazepam.e_commerce.enums.OrderStatus;
import com.ogidazepam.e_commerce.enums.PaymentStatus;
import com.ogidazepam.e_commerce.exceptions.ResourceNotFoundException;
import com.ogidazepam.e_commerce.model.*;
import com.ogidazepam.e_commerce.repository.OrdersRepository;
import com.ogidazepam.e_commerce.repository.PaymentRepository;
import com.ogidazepam.e_commerce.repository.ProductRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {

    private final StripeService stripeService;
    private final OrderValidator orderValidator;
    private final OrdersRepository ordersRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;

    public PaymentService(@Value("${stripe.secret}") String stripeSecret, StripeService stripeService, OrderValidator orderValidator, OrdersRepository ordersRepository, ProductRepository productRepository, PaymentRepository paymentRepository) {
        Stripe.apiKey = stripeSecret;
        this.orderValidator = orderValidator;
        this.stripeService = stripeService;
        this.ordersRepository = ordersRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
    }

    public String checkout(Customer customer, OrderByIdDTO dto) throws StripeException {
        Orders order = ordersRepository.findByIdAndCustomerId(dto.orderId(), customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        //* Check if order has already been completed
        orderValidator.validateNotCompleted(order);

        //* If payment already exists, then change the status to EXPIRED
        markAsExpiredIfPaymentExists(order.getId());

        //* Check for product's stock
        // 1. Get all productIds from OrderItem
        List<Long> productIds = order.getOrderItemList().stream()
                .map(OrderItem::getProductId)
                .toList();
        // 2. Find all products by productIds and store them to the map
        Map<Long, Product> productMap = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 3. Check if each product has enough items in stock
        orderValidator.validateStock(order.getOrderItemList(), productMap);

        //* Create checkout session
        Session session = stripeService.createSession(order);

        //* Save the payment
        Payment payment = Payment.builder()
                .stripeSessionId(session.getId())
                .status(PaymentStatus.CREATED)
                .amount(session.getAmountTotal())
                .currency(session.getCurrency())
                .order(order)
                .build();

        paymentRepository.save(payment);
        return session.getUrl();
    }

    private void markAsExpiredIfPaymentExists(long orderId){
        Optional<Payment> existing = paymentRepository.findFirstByOrderIdAndStatusOrderByCreatedAtDesc(orderId, PaymentStatus.CREATED);
        existing.ifPresent(p -> p.setStatus(PaymentStatus.EXPIRED));
    }

    public void handleSucceededEvent(String sessionId){
        Payment payment = paymentRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        payment.setStatus(PaymentStatus.SUCCESS);
        Orders order = payment.getOrder();
        order.setStatus(OrderStatus.COMPLETED);

        // Reduce each product's quantity
        List<Long> productIds = order.getOrderItemList().stream()
                .map(OrderItem::getProductId)
                .toList();

        Map<Long, Product> productMap = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for(OrderItem orderItem : order.getOrderItemList()){
            Product product = productMap.get(orderItem.getProductId());

            product.setQuantity(product.getQuantity() - orderItem.getQuantity());
        }
        ordersRepository.save(order);
    }

    public void handleFailedEvent(long orderId) {
        Payment payment = paymentRepository.findFirstByOrderIdAndStatusOrderByCreatedAtDesc(orderId, PaymentStatus.CREATED)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        payment.setStatus(PaymentStatus.FAILED);
    }
}