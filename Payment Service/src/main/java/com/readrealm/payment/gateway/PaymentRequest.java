package com.readrealm.payment.gateway;

import java.math.BigDecimal;

public record PaymentRequest(String id,
                             String orderId,
                             BigDecimal amount,
                             String clientSecret
                             ) {
}
