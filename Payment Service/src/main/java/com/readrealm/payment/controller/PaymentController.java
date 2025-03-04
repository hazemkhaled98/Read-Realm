package com.readrealm.payment.controller;

import com.readrealm.payment.dto.PaymentRequest;
import com.readrealm.payment.dto.PaymentResponse;
import com.readrealm.payment.dto.PaymentUpdate;
import com.readrealm.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        PaymentResponse response = paymentService.createPayment(paymentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @Valid @RequestBody PaymentUpdate paymentUpdate) {
        PaymentResponse response = paymentService.updatePaymentStatus(paymentUpdate);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@RequestParam String orderId) {
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(@RequestParam String orderId) {
        PaymentResponse response = paymentService.cancelPayment(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@RequestParam String orderId) {
        PaymentResponse response = paymentService.refundPayment(orderId);
        return ResponseEntity.ok(response);
    }
}