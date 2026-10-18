package com.gridstock.repository;

import com.gridstock.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Inventory entity.
 * Provides CRUD operations and custom queries for inventory data.
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    // Find inventory by SKU
    Inventory findBySku(String sku);
    
    // Find all inventory items with low stock (quantity <= minThreshold)
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.minThreshold")
    List<Inventory> findLowStockItems();
    
    // Find inventory by bin location
    Inventory findByBinLocationId(Long binLocationId);
}
