package com.readrealm.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readrealm.payment.dto.PaymentRequest;
import com.readrealm.payment.dto.PaymentResponse;
import com.readrealm.payment.dto.StripeWebhookRequest;
import com.readrealm.payment.event.ConfirmPaymentEvent;
import com.readrealm.payment.mapper.PaymentMapper;
import com.readrealm.payment.model.Payment;
import com.readrealm.payment.model.PaymentStatus;
import com.readrealm.payment.repository.PaymentRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, ConfirmPaymentEvent> kafkaTemplate;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    public PaymentResponse createPayment(PaymentRequest paymentRequest) {

        if (paymentRepository.existsByOrderId(paymentRequest.orderId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Payment already exists for order: " + paymentRequest.orderId());
        }

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(paymentRequest.amount().multiply(new java.math.BigDecimal(100))
                        .longValue())
                .setCurrency(paymentRequest.currency())
                .putMetadata("orderId", paymentRequest.orderId())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .build();

        try {
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            Payment payment = paymentMapper.toPayment(paymentRequest, paymentIntent.getId(), PaymentStatus.PENDING);
            Payment savedPayment = paymentRepository.save(payment);
            return paymentMapper.toPaymentResponse(savedPayment, paymentIntent.getClientSecret());
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode()), formatStripeError(e.getMessage()));
        }

    }

    public PaymentResponse getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(paymentMapper::toPaymentResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Payment not found for order: " + orderId));
    }


    public void handleStripeWebhook(String requestSignature, String webhookPayload){

        try {
            Event event = Webhook.constructEvent(webhookPayload, requestSignature, stripeWebhookSecret);
            String eventType = event.getType();

            if ("payment_intent.succeeded".equals(eventType)) {
                StripeWebhookRequest stripeWebhookRequest = objectMapper.readValue(webhookPayload, StripeWebhookRequest.class);
                String orderId = stripeWebhookRequest.data().setupIntent().metadata().get("orderId");
                Payment payment = paymentRepository.findByOrderId(orderId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Payment not found for order: " + orderId));

                ConfirmPaymentEvent confirmPaymentEvent = new ConfirmPaymentEvent(orderId);
                kafkaTemplate.send("order-confirmation", confirmPaymentEvent);

                payment.setStatus(PaymentStatus.COMPLETED);
                paymentRepository.save(payment);
            }
        } catch (SignatureVerificationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Stripe signature");
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing Stripe webhook");
        }
    }

    public PaymentResponse cancelPayment(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Payment not found for order: " + orderId));

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getStripePaymentIntentId());
            paymentIntent.cancel();
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode()), formatStripeError(e.getMessage()));
        }

        payment.setStatus(PaymentStatus.CANCELED);


        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    public PaymentResponse refundPayment(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Payment not found for order: " + orderId));

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getStripePaymentIntentId());
            Map<String, Object> params = new HashMap<>();
            params.put("payment_intent", paymentIntent.getId());
            Refund.create(params);
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode()), formatStripeError(e.getMessage()));
        }

        payment.setStatus(PaymentStatus.REFUNDED);


        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    private String formatStripeError(String stripeMessage) {

        int semicolonIndex = stripeMessage.indexOf(';');

        if (semicolonIndex > 0) {
            stripeMessage = stripeMessage.substring(0, semicolonIndex);
        }

        stripeMessage = stripeMessage.replaceAll("\\(pi_[A-Za-z0-9]+\\)", "");

        stripeMessage = stripeMessage.replaceAll("\\s+", " ").trim();

        log.error(stripeMessage);

        return stripeMessage;
    }
}