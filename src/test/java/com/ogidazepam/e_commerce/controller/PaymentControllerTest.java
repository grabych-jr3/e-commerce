package com.ogidazepam.e_commerce.controller;

import com.ogidazepam.e_commerce.config.SecurityConfig;
import com.ogidazepam.e_commerce.dto.OrderByIdDTO;
import com.ogidazepam.e_commerce.enums.UserRole;
import com.ogidazepam.e_commerce.model.Customer;
import com.ogidazepam.e_commerce.service.PaymentService;
import com.ogidazepam.e_commerce.service.StripeService;
import com.ogidazepam.e_commerce.service.security.CustomUserDetailsService;
import com.ogidazepam.e_commerce.service.security.JwtService;
import com.ogidazepam.e_commerce.util.CustomUserDetails;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeError;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(SecurityConfig.class)
public class PaymentControllerTest {

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private StripeService stripeService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Customer customer;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp(){
        customer = Customer.builder().id(1L).role(UserRole.USER).build();
        userDetails = new CustomUserDetails(customer);
    }

    @Test
    void shouldReturnCheckoutPage() throws Exception {
        OrderByIdDTO dto = new OrderByIdDTO(1L);
        String url = "https://stripe.com";

        when(paymentService.checkout(customer, dto)).thenReturn(url);

        mockMvc.perform(post("/api/v1/payments/checkout")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(url));
        verify(paymentService, times(1)).checkout(customer, dto);
    }

    @Test
    void shouldHandleSuccessfulPayment() throws Exception {
        String payload = "{}";
        String header = "sig";

        Event event = mock(Event.class);
        Session session = mock(Session.class);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);

        when(stripeService.constructEvent(payload, header)).thenReturn(event);
        when(event.getType()).thenReturn("checkout.session.completed");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(session));
        when(session.getId()).thenReturn("session123");

        mockMvc.perform(post("/api/v1/payments/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .header("Stripe-Signature", header))
                .andExpect(status().isOk());
        verify(paymentService, times(1)).handleSucceededEvent("session123");
    }

    @Test
    void shouldHandleFailedPayment() throws Exception{
        String payload = "{}";
        String header = "sig";

        Event event = mock(Event.class);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        PaymentIntent paymentIntent = mock(PaymentIntent.class);
        Map<String, String> metadata = Map.of("orderId", "123");
        StripeError stripeError = mock(StripeError.class);

        when(stripeService.constructEvent(payload, header)).thenReturn(event);
        when(event.getType()).thenReturn("payment_intent.payment_failed");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(paymentIntent));
        when(paymentIntent.getMetadata()).thenReturn(metadata);
        when(paymentIntent.getLastPaymentError()).thenReturn(stripeError);
        when(stripeError.getMessage()).thenReturn("Some errors");


        mockMvc.perform(post("/api/v1/payments/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .header("Stripe-Signature", header))
                .andExpect(status().isOk());

        verify(paymentService, times(1)).handleFailedEvent(123L);
    }
}