package com.readrealm.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record OrderRequest(
        @NotNull(message = "User ID must be provided")
        @Positive(message = "User ID must be positive")
        Integer userId,

        @NotEmpty(message = "Order details list cannot be empty")
        List<@Valid Details> details
) {
}
