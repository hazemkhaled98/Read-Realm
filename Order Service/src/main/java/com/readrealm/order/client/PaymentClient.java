package com.readrealm.order.client;

import com.readrealm.order.model.backend.payment.PaymentRequest;
import com.readrealm.order.model.backend.payment.PaymentResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.PostExchange;


public interface PaymentClient {

        @PostExchange("/v1/payments")
        PaymentResponse processPayment(@RequestBody PaymentRequest paymentRequest);

        @PostExchange("v1/payments/cancel")
        PaymentResponse cancelPayment(@RequestParam String orderId);

        @PostExchange("v1/payments/refund")
        PaymentResponse refundPayment(@RequestParam String orderId);
}