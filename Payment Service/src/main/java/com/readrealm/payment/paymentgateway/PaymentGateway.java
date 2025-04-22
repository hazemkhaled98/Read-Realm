package com.readrealm.payment.paymentgateway;

import java.math.BigDecimal;

public interface PaymentGateway {

    PaymentRequest createPaymentRequest(String orderId, BigDecimal amount) throws Exception;
    void cancelPayment(String id) throws Exception;
    void refundPayment(String id) throws Exception;
    default void handleWebhook(String payload, String signature) {}
}
