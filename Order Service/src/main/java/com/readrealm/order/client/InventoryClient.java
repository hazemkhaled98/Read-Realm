package com.readrealm.order.client;

import com.readrealm.order.model.backend.inventory.InventoryRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface InventoryClient {

        @PostExchange("/v1/inventory/reserve-stock")
        void reserveInventory(@RequestBody List<InventoryRequest> inventoryRequest);
}