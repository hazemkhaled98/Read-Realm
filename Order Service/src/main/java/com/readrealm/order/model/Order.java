package com.readrealm.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {
        @Id
        private String orderId;
        private String userId;
        private BigDecimal totalAmount;
        private List<OrderItem> orderItems;
        private PaymentStatus paymentStatus;

        @CreatedDate
        private Instant createdDate;

        @LastModifiedDate
        private Instant updatedDate;
}