package com.readrealm.payment.dto;

import com.readrealm.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String orderId,
        String paymentRequestId,
        BigDecimal amount,
        PaymentStatus status,
        String clientSecret,
        Instant createdDate,
        Instant updatedDate
) {
}