package com.readrealm.order.model.backend.payment;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse(
        String id,
        String orderId,
        String stripePaymentIntentId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String clientSecret
) {}