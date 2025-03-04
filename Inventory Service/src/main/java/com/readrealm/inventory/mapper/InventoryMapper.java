package com.readrealm.inventory.mapper;

import com.readrealm.inventory.dto.InventoryDTO;
import com.readrealm.inventory.model.Inventory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    Inventory toInventory(InventoryDTO request);

    InventoryDTO toInventoryDTO(Inventory inventory);
}