package com.readrealm.inventory.controller;

import com.readrealm.inventory.dto.InventoryRequest;
import com.readrealm.inventory.dto.InventoryResponse;
import com.readrealm.inventory.service.InventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {
        private final InventoryService inventoryService;

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public InventoryResponse createInventory(@Valid @RequestBody InventoryRequest request) {
                return inventoryService.createInventory(request);
        }

        @PutMapping
        public InventoryResponse updateInventory(@Valid @RequestBody InventoryRequest request) {
                return inventoryService.updateInventory(request);
        }

        @GetMapping("/{isbn}")
        public InventoryResponse getInventoryByIsbn(
                        @PathVariable @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$", message = "Invalid ISBN format") String isbn) {
                return inventoryService.getInventoryByIsbn(isbn);
        }

        @DeleteMapping("/{isbn}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void deleteInventory(
                        @PathVariable @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$", message = "Invalid ISBN format") String isbn) {
                inventoryService.deleteInventory(isbn);
        }
}