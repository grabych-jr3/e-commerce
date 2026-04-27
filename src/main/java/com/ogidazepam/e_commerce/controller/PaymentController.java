package com.ogidazepam.e_commerce.controller;

import com.ogidazepam.e_commerce.dto.OrderByIdDTO;
import com.ogidazepam.e_commerce.service.PaymentService;
import com.ogidazepam.e_commerce.service.StripeService;
import com.ogidazepam.e_commerce.util.CustomUserDetails;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    @Value("${stripe.webhook.signing.secret}")
    private String signingSecret;
    private final PaymentService paymentService;
    private final StripeService stripeService;

    @PostMapping("/checkout")
    public String checkout(@AuthenticationPrincipal CustomUserDetails userDetails,
                           @RequestBody OrderByIdDTO dto) throws StripeException {
        return paymentService.checkout(userDetails.customer(), dto);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhookEvent(@RequestBody String payload,
                                                   @RequestHeader("Stripe-Signature") String header) throws SignatureVerificationException {
        Event event = stripeService.constructEvent(payload, header);

        switch (event.getType()) {

            case "checkout.session.completed":

                Session session = (Session) event.getDataObjectDeserializer()
                        .getObject()
                        .orElse(null);
                if (session != null) {
                    paymentService.handleSucceededEvent(session.getId());
                    System.out.println("PAID");
                }
                break;
            case "payment_intent.payment_failed":

                var paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if(paymentIntent != null){
                    String orderId = paymentIntent.getMetadata().get("orderId");
                    paymentService.handleFailedEvent(Long.parseLong(orderId));
                    System.out.println("FAILED");
                    System.out.println("Reason: " + paymentIntent.getLastPaymentError().getMessage());
                }

            default:
                System.out.println("Unhandled type: " + event.getType());
        }
        return ResponseEntity.ok().build();
    }
}
