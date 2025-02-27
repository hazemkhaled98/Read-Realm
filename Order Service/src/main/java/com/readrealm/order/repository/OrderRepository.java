package com.readrealm.order.repository;

import com.readrealm.order.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
        Order findByOrderId(String orderId);

        List<Order> findByUserId(Integer userId);
}