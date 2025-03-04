package com.readrealm.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.ISBN;

public record Details(
        @NotBlank(message = "ISBN cannot be blank")
        @ISBN(message = "Invalid ISBN format")
        String isbn,

        @NotNull(message = "Quantity must be provided")
        @Positive(message = "Quantity must be positive")
        int quantity,

        @NotNull(message = "Price must be provided")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        double price) {
}
