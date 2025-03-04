package com.readrealm.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetails {
        private String isbn;
        private int quantity;
        private BigDecimal unitPrice;
}