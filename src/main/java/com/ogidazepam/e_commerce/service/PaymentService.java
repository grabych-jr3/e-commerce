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
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {

    private final String STRIPE_SECRET;
    private final CartService cartService;
    private final OrdersRepository ordersRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;

    public PaymentService(@Value("${stripe.secret}") String stripeSecret, CartService cartService, OrdersRepository ordersRepository, ProductRepository productRepository, PaymentRepository paymentRepository) {
        STRIPE_SECRET = stripeSecret;
        this.cartService = cartService;
        this.ordersRepository = ordersRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
    }

    public String checkout(Customer customer, OrderByIdDTO dto) throws StripeException {
        Stripe.apiKey = STRIPE_SECRET;

        Orders order = ordersRepository.findByIdAndCustomerId(dto.orderId(), customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if(order.getStatus().equals(OrderStatus.COMPLETED)){
            throw new OrderAlreadyPaidException("Your order " + order.getId() + " is already paid");
        }

        Optional<Payment> existing = paymentRepository.findFirstByOrderIdAndStatusOrderByCreatedAtDesc(order.getId(), PaymentStatus.CREATED);
        existing.ifPresent(p -> p.setStatus(PaymentStatus.EXPIRED));

        List<Long> productIds = order.getOrderItemList().stream()
                .map(OrderItem::getProductId)
                .toList();

        Map<Long, Product> productMap = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (OrderItem orderItem : order.getOrderItemList()){
            Product product = productMap.get(orderItem.getProductId());

            if (product.getQuantity() < orderItem.getQuantity()){
                throw new ProductOutOfStockException("Not enough product quantity in the store");
            }
        }

        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        for(OrderItem orderItem : order.getOrderItemList()){
            lineItems.add(
                    SessionCreateParams.LineItem.builder()
                            .setQuantity((long) orderItem.getQuantity())
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency("pln")
                                            .setUnitAmount((long)orderItem.getUnitPrice()*100)
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName(orderItem.getProductName())
                                                            .build()
                                            )
                                            .build()
                            )
                            .build()
            );
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/success")
                .setCancelUrl("http://localhost:8080/cancel")
                .putMetadata("orderId", String.valueOf(order.getId()))
                .setPaymentIntentData(
                        SessionCreateParams.PaymentIntentData.builder()
                                .putMetadata("orderId", String.valueOf(order.getId()))
                                .build()
                )
                .addAllLineItem(lineItems)
                .build();

        Session session = Session.create(params);

        Payment payment = new Payment(
                session.getId(),
                PaymentStatus.CREATED,
                session.getAmountTotal(),
                session.getCurrency(),
                order
        );

        paymentRepository.save(payment);
        return session.getUrl();
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