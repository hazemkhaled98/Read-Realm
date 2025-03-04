package com.readrealm.order.client;

import com.readrealm.order.model.backend.inventory.InventoryRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "inventoryClient", url = "http://localhost:8082")
public interface InventoryClient {

        @PostMapping("/v1/inventory/reserve-stock")
        void reserveInventory(@RequestBody List<InventoryRequest> inventoryRequest);
}