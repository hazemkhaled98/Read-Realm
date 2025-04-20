package com.readrealm.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.ISBN;

import java.math.BigDecimal;


public record OrderItems(
        @NotBlank(message = "ISBN cannot be blank")
        @ISBN(message = "Invalid ISBN format")
        String isbn,

        @NotNull(message = "Quantity must be provided")
        @Positive(message = "Quantity must be positive")
        int quantity,

        BigDecimal unitPrice
) {
}
