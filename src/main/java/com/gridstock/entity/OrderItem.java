package com.gridstock.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

/**
 * Represents a single item within an order.
 * Each order item links to an inventory item and specifies the quantity needed.
 * The bin location of the inventory item is used by our path optimization algorithm
 * to determine the picking route.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The order this item belongs to
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    // The inventory item to be picked
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    // Quantity of this item needed for the order
    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    // Quantity already picked
    @Column(nullable = false)
    private Integer quantityPicked = 0;

    // Whether this item has been fully picked
    @Column(nullable = false)
    private Boolean isPicked = false;

    // Default constructor for JPA
    public OrderItem() {}

    // Constructor with required fields
    public OrderItem(Order order, Inventory inventory, Integer quantity) {
        this.order = order;
        this.inventory = inventory;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getQuantityPicked() { return quantityPicked; }
    public void setQuantityPicked(Integer quantityPicked) { this.quantityPicked = quantityPicked; }

    public Boolean getIsPicked() { return isPicked; }
    public void setIsPicked(Boolean isPicked) { this.isPicked = isPicked; }

    // Calculate line item total
    public Double getLineTotal() {
        return quantity * inventory.getPrice();
    }

    // Check if item is fully picked
    public boolean isFullyPicked() {
        return quantityPicked >= quantity;
    }

    // Pick a quantity of this item
    public void pickQuantity(Integer qty) {
        this.quantityPicked += qty;
        if (this.quantityPicked >= this.quantity) {
            this.isPicked = true;
            this.quantityPicked = this.quantity;
        }
    }
}
