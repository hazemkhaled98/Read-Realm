package com.readrealm.payment.gateway.stripe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readrealm.payment.dto.StripeWebhookRequest;
import com.readrealm.payment.gateway.PaymentRequest;
import com.readrealm.payment.gateway.WebhookPaymentGateway;
import com.readrealm.payment.model.PaymentStatus;
import com.readrealm.payment.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;

import static com.readrealm.payment.model.PaymentStatus.COMPLETED;
import static com.readrealm.payment.model.PaymentStatus.REQUIRES_PAYMENT_METHOD;


@Slf4j
@Component
public class StripePaymentGateway implements WebhookPaymentGateway {

    private final ObjectMapper objectMapper;

    @Value("${stripe.api.key}")
    private String apiKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    public StripePaymentGateway(ObjectMapper objectMapper) {
        Stripe.apiKey = apiKey;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentRequest createPaymentRequest(String orderId, BigDecimal amount) throws Exception {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount.multiply(new BigDecimal(100))
                        .longValue())
                .setCurrency("usd")
                .putMetadata("orderId", orderId)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .build();

        PaymentIntent stripePaymentIntent = PaymentIntent.create(params);
        return new PaymentRequest(stripePaymentIntent.getId(), orderId,
                amount, stripePaymentIntent.getClientSecret());
    }

    @Override
    public void cancelPayment(String id) throws Exception{
        PaymentIntent paymentIntent = PaymentIntent.retrieve(id);
        paymentIntent.cancel();
    }

    @Override
    public void refundPayment(String id) throws Exception{
        PaymentIntent paymentIntent = PaymentIntent.retrieve(id);
        Refund.create(Map.of("payment_intent", paymentIntent.getId()));
    }

    @Override
    public void handleWebhook(PaymentService paymentService, String webhookPayload, String requestSignature) {

        try {
            Event event = Webhook.constructEvent(webhookPayload, requestSignature, webhookSecret);
            String eventType = event.getType();
            StripeWebhookRequest stripeWebhookRequest = objectMapper.readValue(webhookPayload, StripeWebhookRequest.class);
            String orderId = stripeWebhookRequest.data().setupIntent().metadata().get("orderId");
            if ("payment_intent.succeeded".equals(eventType) || "payment_intent.payment_failed".equals(eventType)) {
               PaymentStatus paymentStatus;
                if ("payment_intent.succeeded".equals(eventType)) {
                    paymentStatus = COMPLETED;
                } else {
                    paymentStatus = REQUIRES_PAYMENT_METHOD;
                }

                paymentService.processWebhookEvent(orderId, paymentStatus);
            }
        } catch (SignatureVerificationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Stripe signature");
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing Stripe webhook");
        }
    }
}
