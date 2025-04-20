package com.readrealm.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderRequest(

        @NotEmpty(message = "Order items list cannot be empty")
        List<@Valid OrderItems> orderItems
) {
}
