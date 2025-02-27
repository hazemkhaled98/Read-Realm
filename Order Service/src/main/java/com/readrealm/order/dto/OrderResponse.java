package com.readrealm.order.dto;

import jakarta.validation.constraints.NotNull;

public record OrderResponse(
        @NotNull(message = "Order ID must be provided")
        String orderId,


        OrderRequest orderRequest

) {
}
