package com.ogidazepam.e_commerce.dto;

import com.ogidazepam.e_commerce.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderViewDTO(OrderStatus status, @NotNull double totalAmount) {
}
