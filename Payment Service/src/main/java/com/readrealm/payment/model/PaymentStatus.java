package com.readrealm.payment.model;

public enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REQUIRES_PAYMENT_METHOD,
    CANCELED,
    REFUNDED
}
