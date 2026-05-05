package com.ogidazepam.e_commerce.repository;

import com.ogidazepam.e_commerce.enums.PaymentStatus;
import com.ogidazepam.e_commerce.model.Orders;
import com.ogidazepam.e_commerce.model.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    private Orders orders;
    private Payment payment1;
    private Payment payment2;

    @BeforeEach
    void setUp(){
        this.orders = Orders.builder()
                .totalAmount(100.00)
                .build();

        this.payment1 = Payment.builder()
                .stripeSessionId("session321")
                .order(orders)
                .status(PaymentStatus.CREATED)
                .currency("usd")
                .amount(100L)
                .build();

        this.payment2 = Payment.builder()
                .stripeSessionId("session123")
                .order(orders)
                .status(PaymentStatus.CREATED)
                .currency("usd")
                .amount(100L)
                .build();
    }

    @Test
    void whenFindFirstPaymentByOrderIdAndPaymentStatusOrderByCreatedAt_shouldReturnFirstPayment() throws InterruptedException {
        // Given
        ordersRepository.save(orders);
        paymentRepository.save(payment1);
        paymentRepository.save(payment2);

        long oderId = orders.getId();
        PaymentStatus status = PaymentStatus.CREATED;

        // When
        Payment paymentFound = paymentRepository.findFirstByOrderIdAndStatusOrderByCreatedAtDesc(oderId, status).get();

        // Then
        System.out.println(payment1.getCreatedAt());
        System.out.println(payment2.getCreatedAt());
        assertThat(paymentFound).isNotNull();
        assertThat(paymentFound.getStripeSessionId()).isEqualTo(payment2.getStripeSessionId());
        assertThat(paymentFound.getStripeSessionId()).isNotEqualTo(payment1.getStripeSessionId());
    }

    @Test
    void whenFindPaymentByStripeSessionId_shouldReturnPayment(){
        // Given
        ordersRepository.save(orders);
        paymentRepository.save(payment1);

        String stripeSessionId = "session321";

        // When
        Payment paymentFound = paymentRepository.findByStripeSessionId(stripeSessionId).get();

        // Then
        assertThat(paymentFound).isNotNull();
        assertThat(paymentFound.getStripeSessionId()).isEqualTo(payment1.getStripeSessionId());
    }
}
