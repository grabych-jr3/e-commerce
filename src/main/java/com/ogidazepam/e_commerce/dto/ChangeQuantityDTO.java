package com.ogidazepam.e_commerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ChangeQuantityDTO(long cartItemId, @NotNull @Min(value = 1) int quantity) {
}
