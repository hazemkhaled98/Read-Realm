package com.readrealm.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.readrealm.order.model.backend.payment.PaymentResponse;
import com.readrealm.order.model.backend.payment.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderResponse(

        String orderId,

        Integer userId,

        BigDecimal totalAmount,

        List<Details> details,

        PaymentStatus paymentStatus,

        PaymentResponse paymentDetails

) {
}
