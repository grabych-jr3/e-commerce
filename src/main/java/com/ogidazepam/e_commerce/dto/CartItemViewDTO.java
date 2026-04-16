package com.ogidazepam.e_commerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemViewDTO(long cartItemId,
                              @NotNull @Min(value = 1) int quantity,
                              @NotNull double price) {
}
