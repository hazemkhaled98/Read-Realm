package com.readrealm.inventory.service;

import com.readrealm.inventory.dto.InventoryDTO;
import com.readrealm.inventory.mapper.InventoryMapper;
import com.readrealm.inventory.model.Inventory;
import com.readrealm.inventory.repository.InventoryRepository;
import com.readrealm.order.event.OrderEvent;
import com.readrealm.order.event.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static com.readrealm.order.event.InventoryStatus.IN_STOCK;
import static com.readrealm.order.event.InventoryStatus.NOT_FOUND;
import static com.readrealm.order.event.InventoryStatus.OUT_OF_STOCK;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
@Slf4j
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @PreAuthorize("@authorizer.isAdmin()")
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

    @PreAuthorize("@authorizer.isAdmin()")
    public void deleteInventory(String isbn) {
        inventoryRepository.deleteByIsbn(isbn);
    }


    @KafkaListener(topics = "inventory")
    @Transactional(rollbackFor = ResponseStatusException.class)
    public void reserveStock(OrderEvent orderEvent) {

        orderEvent.getOrderItems().forEach(orderItem -> {
            String isbn = orderItem.getIsbn();
            Inventory inventory = inventoryRepository.findByIsbn(isbn)
                    .orElseThrow(() -> {
                        orderItem.setInventoryStatus(NOT_FOUND);
                        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inventory not found for ISBN: " + isbn);
                    });

            int orderItemQuantity = orderItem.getQuantity();
            if (inventory.getQuantity() < orderItemQuantity) {
                orderItem.setInventoryStatus(OUT_OF_STOCK);
                kafkaTemplate.send("orders", orderEvent);
                log.info("Order is cancelled due to insufficient inventory for ISBN: {}", isbn);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient inventory for ISBN: " + isbn);
            }

            orderItem.setInventoryStatus(IN_STOCK);
            inventory.setQuantity(inventory.getQuantity() - orderItemQuantity);
            inventoryRepository.save(inventory);
        });

        kafkaTemplate.send("payments", orderEvent);
        log.info("Order is processed and sent to payments topic: {}", orderEvent);

    }

    @KafkaListener(topics = {"order-cancellation", "order-refund", "payments-failure"})
    public void handleOrderEvent(OrderEvent orderEvent) {

        log.info("Restock items for order: {}", orderEvent);

        orderEvent.getOrderItems().forEach(orderItem -> inventoryRepository.findByIsbn(orderItem.getIsbn())
                .ifPresentOrElse(inventory -> restockInventory(orderItem, inventory),
                        () -> log.warn("Inventory not found for ISBN: {}", orderItem.getIsbn())));
    }

    private void restockInventory(OrderItem orderItem, Inventory inventory) {
        inventory.setQuantity(inventory.getQuantity() + orderItem.getQuantity());
        inventoryRepository.save(inventory);
    }
}