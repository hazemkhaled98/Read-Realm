package com.readrealm.payment.mapper;


import com.readrealm.payment.dto.PaymentRequest;
import com.readrealm.payment.dto.PaymentResponse;
import com.readrealm.payment.model.Payment;
import com.readrealm.payment.model.PaymentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

        @Mapping(target = "id", ignore = true)
        @Mapping(target = "stripePaymentIntentId", source = "paymentIntentId")
        @Mapping(target = "status", source = "status")
        @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
        @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
        @Mapping(target = "errorMessage", ignore = true)
        Payment toPayment(PaymentRequest request, String paymentIntentId, PaymentStatus status);

        @Mapping(target = "clientSecret", ignore = true)
        PaymentResponse toPaymentResponse(Payment payment);

        @Mapping(target = "clientSecret", source = "clientSecret")
        PaymentResponse toPaymentResponse(Payment payment, String clientSecret);
}