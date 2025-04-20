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
        public ResponseEntity<String> createOrder(@RequestBody OrderRequest orderRequest) {
                String response = orderService.createOrder(orderRequest);
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

        @PostMapping("/cancel")
        public ResponseEntity<String> cancelOrder(@RequestParam String orderId) {
                String response = orderService.cancelOrder(orderId);
                return new ResponseEntity<>(response, HttpStatus.CREATED);
        }

        @PostMapping("/refund")
        public ResponseEntity<String> refundOrder(@RequestParam String orderId) {
                String response = orderService.refundOrder(orderId);
                return new ResponseEntity<>(response, HttpStatus.CREATED);
        }

}