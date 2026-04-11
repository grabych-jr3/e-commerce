package com.ogidazepam.e_commerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderItemViewDTO(@NotBlank String productName,
                               @NotNull int quantity,
                               @NotNull double price) {
}
