package com.readrealm.payment.controller;

import com.readrealm.payment.dto.PaymentResponse;
import com.readrealm.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@RequestParam String orderId) {
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleStripeWebhook(
            @Valid @RequestBody String webhookPayload,
            @RequestHeader("Stripe-Signature") String stripeSignature
    ) {
        paymentService.handleStripeWebhook(webhookPayload, stripeSignature);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}