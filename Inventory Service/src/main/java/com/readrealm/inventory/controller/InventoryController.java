package com.readrealm.inventory.controller;

import com.readrealm.inventory.dto.InventoryDTO;
import com.readrealm.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.ISBN;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryDTO createInventory(@Valid @RequestBody InventoryDTO request) {
        return inventoryService.createInventory(request);
    }

    @PatchMapping
    public InventoryDTO updateInventory(@Valid @RequestBody InventoryDTO request) {
        return inventoryService.updateInventory(request);
    }

    @GetMapping("/{isbn}")
    public InventoryDTO getInventoryByIsbn(@PathVariable @ISBN(message = "ISBN is invalid") String isbn) {
        return inventoryService.getInventoryByIsbn(isbn);
    }

    @DeleteMapping("/{isbn}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInventory(@PathVariable @ISBN(message = "ISBN is invalid") String isbn) {
        inventoryService.deleteInventory(isbn);
    }

    @PostMapping("/reserve-stock")
    @ResponseStatus(HttpStatus.CREATED)
    public List<InventoryDTO> reserveStock(@Valid @RequestBody List<InventoryDTO> request) {
        return inventoryService.reserveStock(request);
    }
}