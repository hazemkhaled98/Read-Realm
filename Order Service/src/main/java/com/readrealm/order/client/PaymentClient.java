package com.readrealm.order.client;

import com.readrealm.order.model.backend.payment.PaymentRequest;
import com.readrealm.order.model.backend.payment.PaymentResponse;
import com.readrealm.order.model.backend.payment.PaymentUpdate;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "paymentsClient", url = "http://localhost:8083/v1/payments")
public interface PaymentClient {

        @PostMapping
        PaymentResponse processPayment(@RequestBody PaymentRequest paymentRequest);

        @PatchMapping
        PaymentResponse confirmPayment(PaymentUpdate paymentUpdate);

        @PostMapping("/cancel")
        PaymentResponse cancelPayment(@RequestParam String orderId);

        @PostMapping("/refund")
        PaymentResponse refundPayment(@RequestParam String orderId);
}