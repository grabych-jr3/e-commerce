package com.ogidazepam.e_commerce.model;

import com.ogidazepam.e_commerce.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "payment")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    private String stripeSessionId;

    private PaymentStatus status;

    @NotNull
    private long amount;

    @NotBlank
    private String currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Orders order;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public Payment(String stripeSessionId, PaymentStatus status, long amount, String currency, Orders order) {
        this.stripeSessionId = stripeSessionId;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.order = order;
    }
}
