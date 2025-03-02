package com.readrealm.order.service;

import com.readrealm.order.dto.OrderRequest;
import com.readrealm.order.dto.OrderResponse;
import com.readrealm.order.mapper.OrderMapper;
import com.readrealm.order.model.Order;
import com.readrealm.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.REPEATABLE_READ)
public class OrderService {
        private final OrderRepository orderRepository;
        private final OrderMapper orderMapper;

        public OrderResponse createOrder(OrderRequest orderDTO) {
                Order order = orderMapper.toOrder(orderDTO);
                return orderMapper.toOrderResponse(orderRepository.save(order));
        }

        @Transactional(readOnly = true)
        public OrderResponse getOrderById(String orderId) {
                return orderMapper.toOrderResponse(
                        orderRepository.findByOrderId(orderId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with ID: " + orderId)));
        }
        @Transactional(readOnly = true)
        public List<OrderResponse> getOrdersByUserId(Integer userId) {
                List<OrderResponse> orders = orderMapper.toOrderResponseList(orderRepository.findByUserId(userId));
                if (orders.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No orders found for user ID: " + userId);
                }
                return orders;
        }
}