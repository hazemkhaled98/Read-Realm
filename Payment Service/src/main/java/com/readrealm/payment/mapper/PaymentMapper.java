package com.readrealm.payment.mapper;


import com.readrealm.order.event.OrderEvent;
import com.readrealm.payment.dto.PaymentResponse;
import com.readrealm.payment.model.Payment;
import com.readrealm.payment.paymentgateway.PaymentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

        @Mapping(target = "id", ignore = true)
        @Mapping(target = "paymentRequestId", source = "paymentRequest.id")
        @Mapping(target = "status", source = "orderEvent.paymentStatus")
        @Mapping(target = "amount", source = "orderEvent.totalAmount")
        @Mapping(target = "orderEvent" , source = "orderEvent")
        @Mapping(target = "clientSecret", source = "paymentRequest.clientSecret")
        @Mapping(target = "orderId", source = "orderEvent.orderId")
        Payment toPayment(OrderEvent orderEvent, PaymentRequest paymentRequest);


        PaymentResponse toPaymentResponse(Payment payment);
}