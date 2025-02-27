package com.readrealm.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record Details(
        @NotBlank(message = "ISBN cannot be blank")
        @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$",
                message = "Invalid ISBN format")
        String isbn,

        @NotNull(message = "Quantity must be provided")
        @Positive(message = "Quantity must be positive")
        int quantity,

        @NotNull(message = "Price must be provided")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        double price) {
}
