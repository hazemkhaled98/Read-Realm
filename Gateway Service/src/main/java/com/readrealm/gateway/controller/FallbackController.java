package com.readrealm.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import com.readrealm.gateway.dto.ErrorResponse;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/order-service")
    public ResponseEntity<ErrorResponse> orderServiceFallback() {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Order Service is currently unavailable. Please try again later.");
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @GetMapping("/catalog-service")
    public ResponseEntity<ErrorResponse> catalogServiceFallback() {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Catalog Service is currently unavailable. Please try again later.");
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @GetMapping("/create-order")
    public ResponseEntity<ErrorResponse> createOrderFallback() {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "We have a problem processing your order. Please try again later.");
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
