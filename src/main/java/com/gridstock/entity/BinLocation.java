package com.gridstock.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * Represents a specific storage location within a warehouse.
 * Bin locations are the physical positions where inventory items are stored.
 * Each bin has coordinates (row, column) that define its position in the warehouse grid.
 * These coordinates are crucial for our path optimization algorithm.
 */
@Entity
@Table(name = "bin_locations")
public class BinLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique identifier for this bin (e.g., "A-01-03" for Aisle 1, Rack 1, Shelf 3)
    @Column(nullable = false, unique = true)
    private String binCode;

    // Row coordinate in the warehouse grid (0-indexed)
    @Column(name = "grid_row", nullable = false)
    private Integer row;

    // Column coordinate in the warehouse grid (0-indexed)
    @Column(name = "grid_col", nullable = false)
    private Integer column;

    // The warehouse this bin belongs to
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    // Inventory item stored at this bin location
    @OneToOne(mappedBy = "binLocation", cascade = CascadeType.ALL)
    @JsonIgnore
    private Inventory inventory;

    // Default constructor for JPA
    public BinLocation() {}

    // Constructor with required fields
    public BinLocation(String binCode, Integer row, Integer column, Warehouse warehouse) {
        this.binCode = binCode;
        this.row = row;
        this.column = column;
        this.warehouse = warehouse;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBinCode() { return binCode; }
    public void setBinCode(String binCode) { this.binCode = binCode; }

    public Integer getRow() { return row; }
    public void setRow(Integer row) { this.row = row; }

    public Integer getColumn() { return column; }
    public void setColumn(Integer column) { this.column = column; }

    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }

    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }
}
