package com.readrealm.order.controller;

import com.readrealm.order.dto.OrderRequest;
import com.readrealm.order.dto.OrderResponse;
import com.readrealm.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {
        private final OrderService orderService;

        @PostMapping
        public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
                OrderResponse response = orderService.createOrder(orderRequest);
                return new ResponseEntity<>(response, HttpStatus.CREATED);
        }

        @GetMapping("/{orderId}")
        public ResponseEntity<OrderResponse> getOrderById(@PathVariable String orderId) {
                OrderResponse order = orderService.getOrderById(orderId);
                return ResponseEntity.ok(order);
        }

        @GetMapping
        public ResponseEntity<List<OrderResponse>> getUserOrders(@RequestParam Integer userId) {
                List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
                return ResponseEntity.ok(orders);
        }
}