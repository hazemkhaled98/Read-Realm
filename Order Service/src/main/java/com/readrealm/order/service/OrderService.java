package com.readrealm.order.service;

import com.readrealm.order.dto.OrderRequest;
import com.readrealm.order.dto.OrderResponse;
import com.readrealm.order.mapper.OrderMapper;
import com.readrealm.order.model.Order;
import com.readrealm.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
        private final OrderRepository orderRepository;
        private final OrderMapper orderMapper;

        public OrderResponse createOrder(OrderRequest orderDTO) {
                Order order = orderMapper.toOrder(orderDTO);
                return orderMapper.toOrderResponse(orderRepository.save(order));
        }

        public OrderResponse getOrderById(String orderId) {
                return orderMapper.toOrderResponse(orderRepository.findByOrderId(orderId));
        }

        public List<OrderResponse> getOrdersByUserId(Integer userId) {
                return orderMapper.toOrderResponseList(orderRepository.findByUserId(userId));
        }
}