package com.readrealm.inventory.service;

import com.readrealm.inventory.dto.InventoryRequest;
import com.readrealm.inventory.dto.InventoryResponse;
import com.readrealm.inventory.mapper.InventoryMapper;
import com.readrealm.inventory.model.Inventory;
import com.readrealm.inventory.repository.InventoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    public InventoryResponse createInventory(InventoryRequest request) {
        if(inventoryRepository.existsByIsbn(request.isbn())) {
            throw new IllegalArgumentException("Inventory already exists for ISBN: " + request.isbn());
        }
        Inventory inventory = inventoryMapper.toInventory(request);
        return inventoryMapper.toInventoryResponse(inventoryRepository.save(inventory));
    }

    public InventoryResponse updateInventory(InventoryRequest request) {
        if (!inventoryRepository.existsByIsbn(request.isbn())) {
            throw new EntityNotFoundException("Inventory not found for ISBN: " + request.isbn());
        }
        Inventory inventory = inventoryMapper.toInventory(request);
        return inventoryMapper.toInventoryResponse(inventoryRepository.save(inventory));
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByIsbn(String isbn) {
        return inventoryMapper.toInventoryResponse(
                inventoryRepository.findByIsbn(isbn)
                        .orElseThrow(() -> new EntityNotFoundException("Inventory not found for ISBN: " + isbn)));
    }

    public void deleteInventory(String isbn) {
        inventoryRepository.deleteByIsbn(isbn);
    }
}