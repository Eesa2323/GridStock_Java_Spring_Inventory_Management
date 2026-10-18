package com.gridstock.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents an inventory item stored in the warehouse.
 * Each inventory item is associated with a specific bin location and tracks
 * the quantity available. This is what warehouse workers will pick for orders.
 */
@Entity
@Table(name = "inventory")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SKU (Stock Keeping Unit) - unique product identifier
    @Column(nullable = false, unique = true)
    private String sku;

    // Human-readable product name
    @NotBlank
    @Column(nullable = false)
    private String productName;

    // Description of the product
    private String description;

    // Current quantity available in stock
    @Min(0)
    @Column(nullable = false)
    private Integer quantity;

    // Minimum threshold for low stock alerts
    @Min(0)
    @Column(nullable = false)
    private Integer minThreshold;

    // The bin location where this inventory is stored
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bin_location_id")
    @JsonIgnore
    private BinLocation binLocation;

    // Price per unit
    @Column(nullable = false)
    private Double price;

    // Default constructor for JPA
    public Inventory() {}

    // Constructor with required fields
    public Inventory(String sku, String productName, Integer quantity, Integer minThreshold, Double price) {
        this.sku = sku;
        this.productName = productName;
        this.quantity = quantity;
        this.minThreshold = minThreshold;
        this.price = price;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getMinThreshold() { return minThreshold; }
    public void setMinThreshold(Integer minThreshold) { this.minThreshold = minThreshold; }

    public BinLocation getBinLocation() { return binLocation; }
    public void setBinLocation(BinLocation binLocation) { this.binLocation = binLocation; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    // Helper method to check if stock is low
    public boolean isLowStock() {
        return quantity <= minThreshold;
    }
}
