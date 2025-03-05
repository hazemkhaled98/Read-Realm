package com.readrealm.order.client;

import com.readrealm.order.model.backend.payment.PaymentRequest;
import com.readrealm.order.model.backend.payment.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "payment-service")
public interface PaymentClient {

        @PostMapping("/v1/payments")
        PaymentResponse processPayment(@RequestBody PaymentRequest paymentRequest);

        @PostMapping("v1/payments/cancel")
        PaymentResponse cancelPayment(@RequestParam String orderId);

        @PostMapping("v1/payments/refund")
        PaymentResponse refundPayment(@RequestParam String orderId);
}