package com.readrealm.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record OrderRequest(
        @NotNull(message = "User ID must be provided")
        @Positive(message = "User ID must be positive")
        Integer userId,

        @NotNull(message = "Total amount must be provided")
        @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
        double totalAmount,

        @NotEmpty(message = "Order details list cannot be empty")
        List<@Valid Details> details) {
}
