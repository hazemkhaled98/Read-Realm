package com.readrealm.payment.repository;

import com.readrealm.payment.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);
}