package com.readrealm.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderRequest(

        @NotBlank(message = "User ID must be provided")
        String userId,

        @NotEmpty(message = "Order details list cannot be empty")
        List<@Valid Details> details
) {
}
