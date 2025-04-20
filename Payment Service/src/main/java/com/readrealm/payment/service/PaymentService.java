package com.readrealm.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readrealm.order.event.OrderEvent;
import com.readrealm.payment.dto.PaymentResponse;
import com.readrealm.payment.dto.StripeWebhookRequest;
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
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;

import static com.readrealm.order.event.PaymentStatus.CANCELED;
import static com.readrealm.order.event.PaymentStatus.COMPLETED;
import static com.readrealm.order.event.PaymentStatus.PENDING;
import static com.readrealm.order.event.PaymentStatus.REFUNDED;
import static com.readrealm.order.event.PaymentStatus.REQUIRES_PAYMENT_METHOD;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    private static final String ORDERS_TOPIC = "orders";



    public PaymentResponse getPaymentByOrderId(String orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId);
        if (payment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found for order ID: " + orderId);
        }
        return paymentMapper.toPaymentResponse(payment);
    }

    @KafkaListener(topics = "payments")
    public void createPayment(OrderEvent orderEvent) {

        String orderId = orderEvent.getOrderId();
        if (paymentRepository.existsByOrderIdAndStatus(orderId, PaymentStatus.PENDING)) {
            log.warn("Payment already exists for order id: {}, OrderEvent: {}", orderId, orderEvent);
            return;
        }

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(orderEvent.getTotalAmount().multiply(new BigDecimal(100))
                        .longValue())
                .setCurrency("USD")
                .putMetadata("orderId", orderId)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .build();

        PaymentIntent paymentIntent;

        try {
            paymentIntent = PaymentIntent.create(params);
            orderEvent.setPaymentStatus(PENDING);
        } catch (StripeException e) {
            orderEvent.setPaymentStatus(REQUIRES_PAYMENT_METHOD);
            log.error("Payment creation failed for order: {}. Error: {}", orderId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode()), formatStripeError(e.getMessage()));
        }

        Payment payment = paymentMapper.toPayment(orderEvent, paymentIntent.getId(), paymentIntent.getClientSecret());
        paymentRepository.save(payment);

        kafkaTemplate.send(ORDERS_TOPIC, orderEvent);

        log.info("Payment created and sent to orders topic: {}", orderEvent);

    }

    @KafkaListener(topics = "order-cancellation")
    public void cancelPayment(OrderEvent orderEvent) {
        String orderId = orderEvent.getOrderId();
        Payment payment = paymentRepository.findByOrderId(orderId);

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getStripePaymentIntentId());
            paymentIntent.cancel();

            payment.setStatus(PaymentStatus.CANCELED);
            orderEvent.setPaymentStatus(CANCELED);
            payment.setOrderEvent(orderEvent);
            paymentRepository.save(payment);

            kafkaTemplate.send(ORDERS_TOPIC, orderEvent);

            log.info("Payment canceled and sent to orders topic: {}", orderEvent);
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode()), formatStripeError(e.getMessage()));
        }
    }

    @KafkaListener(topics = "order-refund")
    public void refundPayment(OrderEvent orderEvent) {
        String orderId = orderEvent.getOrderId();
        Payment payment = paymentRepository.findByOrderId(orderId);

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getStripePaymentIntentId());
            Refund.create(Map.of("payment_intent", paymentIntent.getId()));

            payment.setStatus(PaymentStatus.REFUNDED);
            orderEvent.setPaymentStatus(REFUNDED);
            payment.setOrderEvent(orderEvent);
            paymentRepository.save(payment);

            kafkaTemplate.send(ORDERS_TOPIC, orderEvent);

            log.info("Payment refunded and sent to orders topic: {}", orderEvent);
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode()), formatStripeError(e.getMessage()));
        }


    }

    public void handleStripeWebhook(String webhookPayload, String requestSignature){

        try {
            Event event = Webhook.constructEvent(webhookPayload, requestSignature, stripeWebhookSecret);
            String eventType = event.getType();


            if ("payment_intent.succeeded".equals(eventType) || "payment_intent.payment_failed".equals(eventType)) {
                StripeWebhookRequest stripeWebhookRequest = objectMapper.readValue(webhookPayload, StripeWebhookRequest.class);
                String orderId = stripeWebhookRequest.data().setupIntent().metadata().get("orderId");
                Payment payment = paymentRepository.findByOrderId(orderId);

                OrderEvent orderEvent = payment.getOrderEvent();

                if("payment_intent.succeeded".equals(eventType)){
                    payment.setStatus(PaymentStatus.COMPLETED);
                    orderEvent.setPaymentStatus(COMPLETED);
                } else {
                    payment.setStatus(PaymentStatus.REQUIRES_PAYMENT_METHOD);
                    orderEvent.setPaymentStatus(REQUIRES_PAYMENT_METHOD);
                }
                paymentRepository.save(payment);

                kafkaTemplate.send(ORDERS_TOPIC, orderEvent);

                log.info("Payment Processed and event sent to orders topic: {}", orderEvent);
            }
        } catch (SignatureVerificationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Stripe signature");
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing Stripe webhook");
        }
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