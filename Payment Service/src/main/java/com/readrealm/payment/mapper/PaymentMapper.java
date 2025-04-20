package com.readrealm.payment.mapper;


import com.readrealm.order.event.OrderEvent;
import com.readrealm.payment.dto.PaymentResponse;
import com.readrealm.payment.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

        @Mapping(target = "id", ignore = true)
        @Mapping(target = "stripePaymentIntentId", source = "paymentIntentId")
        @Mapping(target = "status", source = "orderEvent.paymentStatus")
        @Mapping(target = "amount", source = "orderEvent.totalAmount")
        @Mapping(target = "orderEvent" , source = "orderEvent")
        @Mapping(target = "clientSecret", source = "clientSecret")
        Payment toPayment(OrderEvent orderEvent, String paymentIntentId, String clientSecret);


        PaymentResponse toPaymentResponse(Payment payment);
}