package com.readrealm.payment.paymentgateway;

import com.readrealm.payment.service.PaymentService;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Primary
@Profile("test")
public class MockPaymentGateway implements PaymentGateway {
    @Override
    public PaymentRequest createPaymentRequest(String orderId, BigDecimal amount) throws Exception {
        return new PaymentRequest("mockPaymentId", orderId, amount, "mockClientSecret");
    }

    @Override
    public void cancelPayment(String id) throws Exception {
        // do nothing
        // in mock implementation
        // this is just to simulate the behavior of the real payment gateway
    }

    @Override
    public void refundPayment(String id) throws Exception {
        // do nothing
        // in mock implementation
        // this is just to simulate the behavior of the real payment gateway
    }

    @Override
    public void handleWebhook(PaymentService paymentService, String payload, String signature) {
        // do nothing
        // in mock implementation
        // this is just to simulate the behavior of the real payment gateway
    }
}
