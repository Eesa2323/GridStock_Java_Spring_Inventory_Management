package com.gridstock.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer order that needs to be fulfilled.
 * An order contains multiple items that need to be picked from different bin locations.
 * Our path optimization algorithm will calculate the optimal route to pick all items
 * in this order to minimize worker travel time.
 */
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique order number
    @Column(nullable = false, unique = true)
    private String orderNumber;

    // Customer name
    @Column(nullable = false)
    private String customerName;

    // Order status: PENDING, PICKING, COMPLETED, CANCELLED
    @Column(nullable = false)
    private String status = "PENDING";

    // When the order was created
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // When the order was completed
    private LocalDateTime completedAt;

    // All items in this order
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<OrderItem> orderItems = new ArrayList<>();

    // Default constructor for JPA
    public Order() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructor with required fields
    public Order(String orderNumber, String customerName) {
        this.orderNumber = orderNumber;
        this.customerName = customerName;
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status;
        if (status.equals("COMPLETED") && completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }

    // Helper method to add an item to this order
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    // Calculate total order value
    public Double getTotalValue() {
        return orderItems.stream()
            .mapToDouble(item -> item.getQuantity() * item.getInventory().getPrice())
            .sum();
    }
}
