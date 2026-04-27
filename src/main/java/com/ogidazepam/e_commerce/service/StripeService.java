package com.ogidazepam.e_commerce.service;

import com.ogidazepam.e_commerce.model.OrderItem;
import com.ogidazepam.e_commerce.model.Orders;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StripeService {

    @Value("${stripe.webhook.signing.secret}")
    private String signingSecret;

    private SessionCreateParams createSessionCreateParams(long orderId, List<SessionCreateParams.LineItem> lineItems){
        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/success")
                .setCancelUrl("http://localhost:8080/cancel")
                .putMetadata("orderId", String.valueOf(orderId))
                .setPaymentIntentData(
                        SessionCreateParams.PaymentIntentData.builder()
                                .putMetadata("orderId", String.valueOf(orderId))
                                .build()
                )
                .addAllLineItem(lineItems)
                .build();
    }

    private SessionCreateParams.LineItem mapToLineItem(OrderItem item) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity((long) item.getQuantity())
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("pln")
                                .setUnitAmount((long) item.getUnitPrice() * 100)
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(item.getProductName())
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    public Session createSession(Orders order) throws StripeException {
        List<SessionCreateParams.LineItem> lineItems = order.getOrderItemList()
                .stream()
                .map(this::mapToLineItem)
                .toList();

        SessionCreateParams params = createSessionCreateParams(order.getId(), lineItems);

        return Session.create(params);
    }

    public Event constructEvent(String payload, String header) throws SignatureVerificationException {
        return Webhook.constructEvent(payload, header, signingSecret);
    }
}
