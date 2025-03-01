package com.readrealm.inventory.repository;

import com.readrealm.inventory.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByIsbn(String isbn);
    void deleteByIsbn(String isbn);
    boolean existsByIsbn(String isbn);
}