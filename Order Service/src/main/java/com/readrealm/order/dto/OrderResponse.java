package com.readrealm.order.dto;

public record OrderResponse(

        String orderId,

        OrderRequest orderRequest

) {
}
