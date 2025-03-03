package com.readrealm.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentUpdate(

        @NotBlank(message = "Order ID is required")
        String orderId,

        @NotBlank(message = "Status is required")
        String status
) {
}
