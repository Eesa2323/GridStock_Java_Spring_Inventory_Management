package com.gridstock.controller;

import com.gridstock.entity.Inventory;
import com.gridstock.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing inventory.
 * Provides endpoints for CRUD operations on inventory data and low stock alerts.
 */
@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * Get all inventory items.
     * GET /api/inventory
     */
    @GetMapping
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    /**
     * Get inventory items with low stock (quantity <= minThreshold).
     * GET /api/inventory/low-stock
     */
    @GetMapping("/low-stock")
    public List<Inventory> getLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }

    /**
     * Get a specific inventory item by ID.
     * GET /api/inventory/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable Long id) {
        return inventoryRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get inventory by SKU.
     * GET /api/inventory/sku/{sku}
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<Inventory> getInventoryBySku(@PathVariable String sku) {
        Inventory inventory = inventoryRepository.findBySku(sku);
        return inventory != null ? ResponseEntity.ok(inventory) : ResponseEntity.notFound().build();
    }

    /**
     * Create a new inventory item.
     * POST /api/inventory
     */
    @PostMapping
    public Inventory createInventory(@RequestBody Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    /**
     * Update an existing inventory item.
     * PUT /api/inventory/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(@PathVariable Long id, @RequestBody Inventory inventory) {
        return inventoryRepository.findById(id)
            .map(existingInventory -> {
                existingInventory.setSku(inventory.getSku());
                existingInventory.setProductName(inventory.getProductName());
                existingInventory.setDescription(inventory.getDescription());
                existingInventory.setQuantity(inventory.getQuantity());
                existingInventory.setMinThreshold(inventory.getMinThreshold());
                existingInventory.setPrice(inventory.getPrice());
                return ResponseEntity.ok(inventoryRepository.save(existingInventory));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update inventory quantity (e.g., after picking items).
     * PATCH /api/inventory/{id}/quantity
     */
    @PatchMapping("/{id}/quantity")
    public ResponseEntity<?> updateQuantity(@PathVariable Long id, @RequestParam Integer change) {
        return inventoryRepository.findById(id)
            .map(inventory -> {
                int newQuantity = inventory.getQuantity() + change;
                if (newQuantity >= 0) {
                    inventory.setQuantity(newQuantity);
                    return ResponseEntity.ok(inventoryRepository.save(inventory));
                }
                return ResponseEntity.badRequest().build();
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Restock inventory (add quantity).
     * POST /api/inventory/{id}/restock
     */
    @PostMapping("/{id}/restock")
    public ResponseEntity<?> restockInventory(@PathVariable Long id, @RequestParam Integer quantity) {
        return inventoryRepository.findById(id)
            .map(inventory -> {
                inventory.setQuantity(inventory.getQuantity() + quantity);
                return ResponseEntity.ok(inventoryRepository.save(inventory));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete an inventory item.
     * DELETE /api/inventory/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        if (inventoryRepository.existsById(id)) {
            inventoryRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
