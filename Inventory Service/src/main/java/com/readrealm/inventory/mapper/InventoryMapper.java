package com.readrealm.inventory.mapper;

import com.readrealm.inventory.dto.InventoryRequest;
import com.readrealm.inventory.dto.InventoryResponse;
import com.readrealm.inventory.model.Inventory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    Inventory toInventory(InventoryRequest request);

    InventoryResponse toInventoryResponse(Inventory inventory);
}