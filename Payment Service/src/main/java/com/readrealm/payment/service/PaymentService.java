package com.readrealm.payment.service;

import com.readrealm.payment.dto.PaymentRequest;
import com.readrealm.payment.dto.PaymentResponse;
import com.readrealm.payment.dto.PaymentUpdate;
import com.readrealm.payment.mapper.PaymentMapper;
import com.readrealm.payment.model.Payment;
import com.readrealm.payment.model.PaymentStatus;
import com.readrealm.payment.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public PaymentResponse createPayment(PaymentRequest paymentRequest) {

        if(paymentRepository.existsByOrderId(paymentRequest.orderId())) {
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

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(paymentMapper::toPaymentResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Payment not found for order: " + orderId));
    }

    // Only exists as a way to update the status of a payment
    // best practice would be to use webhooks to update the status of a payment
    public PaymentResponse updatePaymentStatus(@Valid PaymentUpdate paymentUpdate) {
        Payment payment = paymentRepository.findByOrderId(paymentUpdate.orderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Payment not found for order: " + paymentUpdate.orderId()));

        PaymentStatus newStatus = switch (paymentUpdate.status()) {
            case "succeeded" -> PaymentStatus.COMPLETED;
            case "processing" -> PaymentStatus.PROCESSING;
            case "requires_payment_method" -> PaymentStatus.REQUIRES_PAYMENT_METHOD;
            case "canceled" -> PaymentStatus.CANCELED;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status: " + paymentUpdate.status());
        };

        payment.setStatus(newStatus);
        payment.setUpdatedAt(LocalDateTime.now());

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
        payment.setUpdatedAt(LocalDateTime.now());

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