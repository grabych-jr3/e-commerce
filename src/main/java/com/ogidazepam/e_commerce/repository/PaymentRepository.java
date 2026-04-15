package com.ogidazepam.e_commerce.repository;

import com.ogidazepam.e_commerce.enums.PaymentStatus;
import com.ogidazepam.e_commerce.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findFirstByOrderIdAndStatusOrderByCreatedAtDesc(long orderId, PaymentStatus paymentStatus);
    Optional<Payment> findByStripeSessionId(String id);
}
