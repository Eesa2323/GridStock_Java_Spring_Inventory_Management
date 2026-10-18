package com.gridstock.controller;

import com.gridstock.dto.WarehouseDTO;
import com.gridstock.entity.Warehouse;
import com.gridstock.repository.BinLocationRepository;
import com.gridstock.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing warehouses.
 * Provides endpoints for CRUD operations on warehouse data.
 */
@RestController
@RequestMapping("/api/warehouses")
@CrossOrigin(origins = "*")
public class WarehouseController {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private BinLocationRepository binLocationRepository;

    /**
     * Get all warehouses.
     * GET /api/warehouses
     */
    @GetMapping
    public List<WarehouseDTO> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
            .map(this::convertToDTO)
            .toList();
    }

    /**
     * Get a specific warehouse by ID.
     * GET /api/warehouses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<WarehouseDTO> getWarehouseById(@PathVariable Long id) {
        return warehouseRepository.findById(id)
            .map(this::convertToDTO)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new warehouse.
     * POST /api/warehouses
     */
    @PostMapping
    public Warehouse createWarehouse(@RequestBody Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    /**
     * Update an existing warehouse.
     * PUT /api/warehouses/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Warehouse> updateWarehouse(@PathVariable Long id, @RequestBody Warehouse warehouse) {
        return warehouseRepository.findById(id)
            .map(existingWarehouse -> {
                existingWarehouse.setName(warehouse.getName());
                existingWarehouse.setRows(warehouse.getRows());
                existingWarehouse.setColumns(warehouse.getColumns());
                existingWarehouse.setLocation(warehouse.getLocation());
                return ResponseEntity.ok(warehouseRepository.save(existingWarehouse));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a warehouse.
     * DELETE /api/warehouses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        if (warehouseRepository.existsById(id)) {
            warehouseRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Helper method to convert Warehouse entity to WarehouseDTO.
     * This explicitly fetches the bin locations to avoid lazy loading issues.
     */
    private WarehouseDTO convertToDTO(Warehouse warehouse) {
        List<com.gridstock.entity.BinLocation> bins = binLocationRepository.findByWarehouseId(warehouse.getId());
        
        List<WarehouseDTO.BinLocationDTO> binDTOs = bins.stream().map(bin -> {
            boolean hasInv = bin.getInventory() != null;
            return new WarehouseDTO.BinLocationDTO(
                bin.getId(),
                bin.getBinCode(),
                bin.getRow(),
                bin.getColumn(),
                hasInv
            );
        }).toList();
        
        return new WarehouseDTO(
            warehouse.getId(),
            warehouse.getName(),
            warehouse.getRows(),
            warehouse.getColumns(),
            warehouse.getLocation(),
            binDTOs
        );
    }
}
