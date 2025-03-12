package com.readrealm.order.model;

import com.readrealm.order.model.backend.payment.PaymentStatus;
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
        private Integer userId;
        private BigDecimal totalAmount;
        private List<OrderDetails> details;
        private PaymentStatus paymentStatus;

        @CreatedDate
        private Instant createdDate;

        @LastModifiedDate
        private Instant updatedDate;
}