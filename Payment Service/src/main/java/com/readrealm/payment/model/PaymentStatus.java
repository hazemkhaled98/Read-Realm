package com.readrealm.payment.model;

public enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    REQUIRES_PAYMENT_METHOD,
    CANCELED,
    FAILED,
    REFUNDED;


    public static PaymentStatus fromString(String text) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.toString().equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No Payment Status with name " + text + " found");
    }
}
