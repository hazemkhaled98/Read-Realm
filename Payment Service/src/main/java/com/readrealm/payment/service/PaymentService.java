package com.readrealm.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.readrealm.order.event.OrderEvent;
import com.readrealm.payment.dto.PaymentResponse;
import com.readrealm.payment.mapper.PaymentMapper;
import com.readrealm.payment.model.Payment;
import com.readrealm.payment.model.PaymentStatus;
import com.readrealm.payment.paymentgateway.PaymentGateway;
import com.readrealm.payment.paymentgateway.PaymentRequest;
import com.readrealm.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static com.readrealm.order.event.PaymentStatus.CANCELED;
import static com.readrealm.order.event.PaymentStatus.COMPLETED;
import static com.readrealm.order.event.PaymentStatus.PENDING;
import static com.readrealm.order.event.PaymentStatus.REFUNDED;
import static com.readrealm.order.event.PaymentStatus.REQUIRES_PAYMENT_METHOD;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentGateway paymentGateway;
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

        try {
            PaymentRequest paymentRequest = paymentGateway.createPaymentRequest(orderId, orderEvent.getTotalAmount());
            orderEvent.setPaymentStatus(PENDING);
            Payment payment = paymentMapper.toPayment(orderEvent, paymentRequest);
            paymentRepository.save(payment);
            log.info("Payment created and sent to orders topic: {}", orderEvent);
        } catch (Exception e) {
            log.error("Payment creation failed for order: {}. Error: {}", orderId, e.getMessage());
            orderEvent.setPaymentStatus(REQUIRES_PAYMENT_METHOD);
        }

        kafkaTemplate.send(ORDERS_TOPIC, orderEvent);

    }

    @KafkaListener(topics = "order-cancellation")
    public void cancelPayment(OrderEvent orderEvent) {
        String orderId = orderEvent.getOrderId();
        Payment payment = paymentRepository.findByOrderId(orderId);

        try {
            paymentGateway.cancelPayment(payment.getPaymentRequestId());

            payment.setStatus(PaymentStatus.CANCELED);
            orderEvent.setPaymentStatus(CANCELED);
            payment.setOrderEvent(orderEvent);
            paymentRepository.save(payment);

            log.info("Payment canceled and sent to orders topic: {}", orderEvent);
        } catch (Exception e) {
            log.warn("Payment canceled for order: {}. Error: {}", orderId, e.getMessage());
        }
        kafkaTemplate.send(ORDERS_TOPIC, orderEvent);
    }

    @KafkaListener(topics = "order-refund")
    public void refundPayment(OrderEvent orderEvent) {
        String orderId = orderEvent.getOrderId();
        Payment payment = paymentRepository.findByOrderId(orderId);

        try {

            paymentGateway.refundPayment(payment.getPaymentRequestId());

            payment.setStatus(PaymentStatus.REFUNDED);
            orderEvent.setPaymentStatus(REFUNDED);
            payment.setOrderEvent(orderEvent);
            paymentRepository.save(payment);

            log.info("Payment refunded and sent to orders topic: {}", orderEvent);
        } catch (Exception e) {
            log.warn("Payment refunding failed for order: {}. Error: {}", orderId, e.getMessage());
        }
        kafkaTemplate.send(ORDERS_TOPIC, orderEvent);
    }

    public void processWebhookEvent(String orderId, PaymentStatus paymentStatus) {

        Payment payment = paymentRepository.findByOrderId(orderId);

        OrderEvent orderEvent = payment.getOrderEvent();
        payment.setStatus(paymentStatus);

        orderEvent.setPaymentStatus(paymentStatus == PaymentStatus.COMPLETED ? COMPLETED : REQUIRES_PAYMENT_METHOD);

        paymentRepository.save(payment);

        kafkaTemplate.send(ORDERS_TOPIC, orderEvent);

        log.info("Payment Processed and event sent to orders topic: {}", orderEvent);
    }
}