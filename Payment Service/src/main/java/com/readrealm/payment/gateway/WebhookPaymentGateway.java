package com.readrealm.payment.gateway;

import com.readrealm.payment.service.PaymentService;

public interface WebhookPaymentGateway extends PaymentGateway {

    void handleWebhook(PaymentService paymentService, String payload, String signature);
}
