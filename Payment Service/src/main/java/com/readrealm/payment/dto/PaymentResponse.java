package com.readrealm.payment.dto;

import com.readrealm.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String id,
        String orderId,
        String stripePaymentIntentId,
        BigDecimal amount,
        PaymentStatus status,
        String clientSecret,
        Instant createdDate,
        Instant updatedDate
) {
}