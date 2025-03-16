package com.readrealm.order.client;

import com.readrealm.order.model.backend.catalog.BookResponse;
import com.readrealm.order.model.backend.payment.PaymentRequest;
import com.readrealm.order.model.backend.payment.PaymentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;


public interface PaymentClient {

        @PostExchange("/v1/payments")
        @CircuitBreaker(name = "payment", fallbackMethod = "fallbackMethod")
        @Retry(name = "payment")
        PaymentResponse processPayment(@RequestBody PaymentRequest paymentRequest);

        @PostExchange("v1/payments/cancel")
        @CircuitBreaker(name = "payment", fallbackMethod = "fallbackMethod")
        @Retry(name = "payment")
        PaymentResponse cancelPayment(@RequestParam String orderId);

        @PostExchange("v1/payments/refund")
        @CircuitBreaker(name = "payment", fallbackMethod = "fallbackMethod")
        @Retry(name = "payment")
        PaymentResponse refundPayment(@RequestParam String orderId);


        default List<BookResponse> fallbackMethod() {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Payment service is down");
        }
}