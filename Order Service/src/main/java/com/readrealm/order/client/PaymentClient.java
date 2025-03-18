package com.readrealm.order.client;

import com.readrealm.order.model.backend.payment.PaymentRequest;
import com.readrealm.order.model.backend.payment.PaymentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.service.annotation.PostExchange;

public interface PaymentClient {

        @CircuitBreaker(name = "paymentCircuitBreaker", fallbackMethod = "processPaymentFallback")
        @Retry(name = "paymentRetry", fallbackMethod = "processPaymentFallback")
        @RateLimiter(name = "paymentRateLimiter", fallbackMethod = "processPaymentFallback")
        @PostExchange("/v1/payments")
        PaymentResponse processPayment(@RequestBody PaymentRequest paymentRequest);

        @CircuitBreaker(name = "paymentCircuitBreaker", fallbackMethod = "cancelPaymentFallback")
        @Retry(name = "paymentRetry", fallbackMethod = "cancelPaymentFallback")
        @RateLimiter(name = "paymentRateLimiter", fallbackMethod = "cancelPaymentFallback")
        @PostExchange("v1/payments/cancel")
        PaymentResponse cancelPayment(@RequestParam String orderId);

        @CircuitBreaker(name = "paymentCircuitBreaker", fallbackMethod = "refundPaymentFallback")
        @Retry(name = "paymentRetry", fallbackMethod = "refundPaymentFallback")
        @RateLimiter(name = "paymentRateLimiter", fallbackMethod = "refundPaymentFallback")
        @PostExchange("v1/payments/refund")
        PaymentResponse refundPayment(@RequestParam String orderId);

        default PaymentResponse processPaymentFallback(PaymentRequest paymentRequest, Exception e) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Payment service is unavailable");
        }

        default PaymentResponse cancelPaymentFallback(String orderId, Exception e) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Payment service is unavailable");
        }

        default PaymentResponse refundPaymentFallback(String orderId, Exception e) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Payment service is unavailable");
        }
}