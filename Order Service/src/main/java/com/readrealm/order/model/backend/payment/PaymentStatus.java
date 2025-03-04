package com.readrealm.order.model.backend.payment;

public enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REQUIRES_PAYMENT_METHOD,
    CANCELED,
    REFUNDED
}
