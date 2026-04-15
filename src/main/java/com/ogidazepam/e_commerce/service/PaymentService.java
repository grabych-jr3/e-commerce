package com.ogidazepam.e_commerce.service;

import com.ogidazepam.e_commerce.enums.OrderStatus;
import com.ogidazepam.e_commerce.enums.PaymentStatus;
import com.ogidazepam.e_commerce.model.*;
import com.ogidazepam.e_commerce.repository.OrdersRepository;
import com.ogidazepam.e_commerce.repository.PaymentRepository;
import com.ogidazepam.e_commerce.repository.ProductRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public String checkout(Customer customer, Map<String, Object> map) throws StripeException {
        Stripe.apiKey = STRIPE_SECRET;

        Orders order = ordersRepository.findByIdAndCustomerId(((Number)map.get("orderId")).longValue(), customer.getId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if(order.getStatus().equals(OrderStatus.COMPLETED)){
            return "The order has been already paid";
        }

        for (OrderItem orderItem : order.getOrderItemList()){
            Product product = productRepository.findById(orderItem.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found"));

            if (product.getQuantity() < orderItem.getQuantity()){
                throw new RuntimeException("Not enough product quantity in the store");
            }
        }

        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        for(OrderItem orderItem : order.getOrderItemList()){
            lineItems.add(
                    SessionCreateParams.LineItem.builder()
                            .setQuantity((long) orderItem.getQuantity())
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency("usd")
                                            .setUnitAmount((long)orderItem.getPrice()*100)
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
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

        payment.setStatus(PaymentStatus.SUCCESS);
        Orders order = payment.getOrder();
        order.setStatus(OrderStatus.COMPLETED);


        // Reduce each product's quantity
        for(OrderItem orderItem : order.getOrderItemList()){
            Product product = productRepository.findById(orderItem.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found"));

            product.setQuantity(product.getQuantity() - orderItem.getQuantity());
        }
        ordersRepository.save(order);

        // Clear the cart
        cartService.clearTheCart(order.getCustomer());
    }

    public void handleFailedEvent(long orderId) {
        Payment payment = paymentRepository.findFirstByOrderIdAndStatusOrderByCreatedAtDesc(orderId, PaymentStatus.CREATED)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

        payment.setStatus(PaymentStatus.FAILED);
    }
}