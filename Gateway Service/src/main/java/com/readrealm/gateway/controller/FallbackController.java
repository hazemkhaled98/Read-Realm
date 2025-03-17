package com.readrealm.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/order-service")
    public ResponseEntity<String> orderServiceFallback() {
        return ResponseEntity.status(503).body("Order Service is currently unavailable. Please try again later.");
    }

    @GetMapping("/catalog-service")
    public ResponseEntity<String> catalogServiceFallback() {
        return ResponseEntity.status(503).body("Catalog Service is currently unavailable. Please try again later.");
    }
}
