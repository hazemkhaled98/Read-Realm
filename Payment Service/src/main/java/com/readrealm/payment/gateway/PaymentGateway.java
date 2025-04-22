package com.readrealm.payment.gateway;

import java.math.BigDecimal;

public interface PaymentGateway {

    PaymentRequest createPaymentRequest(String orderId, BigDecimal amount) throws Exception;
    void cancelPayment(String id) throws Exception;
    void refundPayment(String id) throws Exception;
}
