package com.readrealm.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {
        @Id
        private String id;
        private String orderId;
        private String stripePaymentIntentId;
        private BigDecimal amount;
        private String currency;
        private PaymentStatus status;
        private String errorMessage;

        @CreatedDate
        private Instant createdDate;

        @LastModifiedDate
        private Instant updatedDate;
}