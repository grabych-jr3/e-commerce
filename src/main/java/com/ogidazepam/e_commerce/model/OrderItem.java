package com.ogidazepam.e_commerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "order_item")
@Getter @Setter @NoArgsConstructor @ToString
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    private String productName;

    @NotNull
    @Min(1)
    private int quantity;

    @NotNull
    @Min(0)
    private double price;

    private long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Orders order;

    public OrderItem(String productName, int quantity, double price, Orders order, long productId) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.order = order;
        this.productId = productId;
    }
}
