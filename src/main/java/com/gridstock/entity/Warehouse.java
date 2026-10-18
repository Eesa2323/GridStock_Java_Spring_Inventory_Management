package com.gridstock.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a warehouse in our system.
 * A warehouse has dimensions (rows and columns of aisles) and contains multiple bin locations.
 * This is the top-level entity for organizing our warehouse layout.
 */
@Entity
@Table(name = "warehouses")
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Human-readable name for this warehouse
    @Column(nullable = false)
    private String name;

    // Number of rows in the warehouse grid (vertical dimension)
    @Column(nullable = false)
    private Integer rows;

    // Number of columns in the warehouse grid (horizontal dimension)
    @Column(nullable = false)
    private Integer columns;

    // Physical location/address of the warehouse
    private String location;

    // All bin locations within this warehouse
    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<BinLocation> binLocations = new ArrayList<>();

    // Default constructor for JPA
    public Warehouse() {}

    // Constructor with required fields
    public Warehouse(String name, Integer rows, Integer columns, String location) {
        this.name = name;
        this.rows = rows;
        this.columns = columns;
        this.location = location;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getRows() { return rows; }
    public void setRows(Integer rows) { this.rows = rows; }

    public Integer getColumns() { return columns; }
    public void setColumns(Integer columns) { this.columns = columns; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public List<BinLocation> getBinLocations() { return binLocations; }
    public void setBinLocations(List<BinLocation> binLocations) { this.binLocations = binLocations; }

    // Helper method to add a bin location to this warehouse
    public void addBinLocation(BinLocation binLocation) {
        binLocations.add(binLocation);
        binLocation.setWarehouse(this);
    }
}
