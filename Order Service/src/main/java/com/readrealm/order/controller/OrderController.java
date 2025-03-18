package com.readrealm.order.controller;

import com.readrealm.order.dto.OrderRequest;
import com.readrealm.order.dto.OrderResponse;
import com.readrealm.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class OrderController {
        private final OrderService orderService;

        @PostMapping("/create")
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
        public ResponseEntity<List<OrderResponse>> getUserOrders(@RequestParam String userId) {
                List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
                return ResponseEntity.ok(orders);
        }

        @PostMapping("/confirm")
        public ResponseEntity<OrderResponse> confirmOrder(@RequestParam String orderId) {
                OrderResponse order = orderService.confirmOrder(orderId);
                return ResponseEntity.ok(order);
        }

        @PostMapping("/cancel")
        public ResponseEntity<OrderResponse> cancelOrder(@RequestParam String orderId) {
                OrderResponse order = orderService.cancelOrder(orderId);
                return ResponseEntity.ok(order);
        }

        @PostMapping("/refund")
        public ResponseEntity<OrderResponse> refundOrder(@RequestParam String orderId) {
                OrderResponse order = orderService.refundOrder(orderId);
                return ResponseEntity.ok(order);
        }

}