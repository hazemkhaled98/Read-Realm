package com.readrealm.order.client;

import com.readrealm.order.model.backend.inventory.InventoryRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "inventoryClient", url = "http://localhost:8082/v1/inventory/reserve-stock")
public interface InventoryClient {

        @PostMapping
        void reserveInventory(@RequestBody List<InventoryRequest> inventoryRequest);
}