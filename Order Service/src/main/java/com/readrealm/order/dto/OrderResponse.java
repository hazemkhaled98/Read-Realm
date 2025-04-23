package com.readrealm.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.readrealm.order.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderResponse(

        String orderId,

        String userId,

        BigDecimal totalAmount,

        List<OrderItem> orderItems,

        PaymentStatus paymentStatus,

        Instant createdDate,

        Instant updatedDate

) {
}
