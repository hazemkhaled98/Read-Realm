package com.readrealm.order.model.backend.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.ISBN;

public record InventoryRequest(
        @NotNull(message = "ISBN is required")
        @ISBN(message = "ISBN is invalid")
        String isbn,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than or equal to 1")
        Integer quantity
) {}