package com.readrealm.payment.dto;

import com.readrealm.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String id,
        String orderId,
        String stripePaymentIntentId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        Instant createdDate,
        Instant updatedDate,
        String clientSecret
) {}