package com.readrealm.order.client;

import com.readrealm.order.model.backend.inventory.InventoryRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;


public interface InventoryClient {

        @PostExchange("/v1/inventory/reserve-stock")
        @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
        @Retry(name = "inventory")
        void reserveInventory(@RequestBody List<InventoryRequest> inventoryRequest);


        default void fallbackMethod() {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Inventory service is down");
        }
}