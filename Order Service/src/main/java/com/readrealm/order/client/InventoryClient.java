package com.readrealm.order.client;

import com.readrealm.order.model.backend.inventory.InventoryRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

        @PostMapping("/v1/inventory/reserve-stock")
        void reserveInventory(@RequestBody List<InventoryRequest> inventoryRequest);
}