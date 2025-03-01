package com.readrealm.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record InventoryRequest(
        @NotNull(message = "ISBN is required")
        @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$",
                message = "Invalid ISBN format")
        String isbn,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than or equal to 1")
        Integer quantity
) {}