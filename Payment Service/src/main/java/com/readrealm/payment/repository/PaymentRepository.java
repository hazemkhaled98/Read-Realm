package com.readrealm.payment.repository;

import com.readrealm.payment.model.Payment;
import com.readrealm.payment.model.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    Payment findByOrderId(String orderId);

    boolean existsByOrderIdAndStatus(String orderId, PaymentStatus status);
}