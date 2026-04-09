package com.ogidazepam.e_commerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductViewDTO(@NotBlank String name, @NotBlank String description,
                             @NotNull @Min(value = 1) double price, @NotNull @Min(value = 0) int quantity) {
}
