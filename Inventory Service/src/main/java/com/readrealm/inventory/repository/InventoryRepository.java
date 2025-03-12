package com.readrealm.inventory.repository;

import com.readrealm.inventory.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query(value = "SELECT * FROM Inventory WHERE isbn = :isbn for update", nativeQuery = true)
    Optional<Inventory> findByIsbn(String isbn);

    void deleteByIsbn(String isbn);

    boolean existsByIsbn(String isbn);
}