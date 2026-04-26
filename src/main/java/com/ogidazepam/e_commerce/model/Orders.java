package com.ogidazepam.e_commerce.model;

import com.ogidazepam.e_commerce.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private OrderStatus status;

    @NotNull
    @Min(0)
    private double totalAmount;

    @CreationTimestamp
    private Instant orderDate;

    @UpdateTimestamp
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItemList;

    @OneToMany(mappedBy = "order")
    private List<Payment> paymentList;

    public Orders(OrderStatus status, double totalAmount, Customer customer) {
        this.status = status;
        this.totalAmount = totalAmount;
        this.customer = customer;
    }
}
