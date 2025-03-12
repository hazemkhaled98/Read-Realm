package com.readrealm.inventory.service;

import com.readrealm.inventory.dto.InventoryDTO;
import com.readrealm.inventory.mapper.InventoryMapper;
import com.readrealm.inventory.model.Inventory;
import com.readrealm.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    public InventoryDTO createInventory(InventoryDTO request) {
        if (inventoryRepository.existsByIsbn(request.isbn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inventory already exists for ISBN: " + request.isbn());
        }
        Inventory inventory = inventoryMapper.toInventory(request);
        return inventoryMapper.toInventoryDTO(inventoryRepository.save(inventory));
    }

    public InventoryDTO updateInventory(InventoryDTO request) {

        Inventory inventory = inventoryRepository.findByIsbn(request.isbn())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for ISBN: " + request.isbn()));

        inventory.setQuantity(inventory.getQuantity() + request.quantity());

        return inventoryMapper.toInventoryDTO(inventoryRepository.save(inventory));
    }


    public InventoryDTO getInventoryByIsbn(String isbn) {
        return inventoryMapper.toInventoryDTO(
                inventoryRepository.findByIsbn(isbn)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for ISBN: " + isbn)));
    }

    public void deleteInventory(String isbn) {
        inventoryRepository.deleteByIsbn(isbn);
    }


    @Transactional(rollbackFor = ResponseStatusException.class)
    public List<InventoryDTO> reserveStock(List<InventoryDTO> requests) {
        return requests.stream()
                .map(this::reserveStock)
                .toList();
    }


    private InventoryDTO reserveStock(InventoryDTO request) {
        Inventory inventory = inventoryRepository.findByIsbn(request.isbn())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for ISBN: " + request.isbn()));

        if (inventory.getQuantity() < request.quantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient inventory for ISBN: " + request.isbn());
        }

        inventory.setQuantity(inventory.getQuantity() - request.quantity());

        return inventoryMapper.toInventoryDTO(inventoryRepository.save(inventory));
    }
}