package com.readrealm.payment.paymentgateway;

import java.math.BigDecimal;

public record PaymentRequest(String id,
                             String orderId,
                             BigDecimal amount,
                             String clientSecret
                             ) {
}
