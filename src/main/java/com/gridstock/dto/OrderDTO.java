package com.gridstock.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Order with order items included.
 * This solves the lazy loading issue by explicitly fetching the needed data.
 */
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private String customerName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private List<OrderItemDTO> orderItems;

    public OrderDTO() {}

    public OrderDTO(Long id, String orderNumber, String customerName, String status, 
                    LocalDateTime createdAt, LocalDateTime completedAt, List<OrderItemDTO> orderItems) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.customerName = customerName;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.orderItems = orderItems;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public List<OrderItemDTO> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemDTO> orderItems) { this.orderItems = orderItems; }

    /**
     * Inner DTO for OrderItem to avoid circular references
     */
    public static class OrderItemDTO {
        private Long id;
        private Integer quantity;
        private Integer quantityPicked;
        private Boolean isPicked;
        private InventoryDTO inventory;

        public OrderItemDTO() {}

        public OrderItemDTO(Long id, Integer quantity, Integer quantityPicked, Boolean isPicked, InventoryDTO inventory) {
            this.id = id;
            this.quantity = quantity;
            this.quantityPicked = quantityPicked;
            this.isPicked = isPicked;
            this.inventory = inventory;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public Integer getQuantityPicked() { return quantityPicked; }
        public void setQuantityPicked(Integer quantityPicked) { this.quantityPicked = quantityPicked; }

        public Boolean getIsPicked() { return isPicked; }
        public void setIsPicked(Boolean isPicked) { this.isPicked = isPicked; }

        public InventoryDTO getInventory() { return inventory; }
        public void setInventory(InventoryDTO inventory) { this.inventory = inventory; }
    }

    /**
     * Inner DTO for Inventory to avoid circular references
     */
    public static class InventoryDTO {
        private Long id;
        private String sku;
        private String productName;
        private Integer quantity;
        private Double price;
        private BinLocationDTO binLocation;

        public InventoryDTO() {}

        public InventoryDTO(Long id, String sku, String productName, Integer quantity, Double price, BinLocationDTO binLocation) {
            this.id = id;
            this.sku = sku;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
            this.binLocation = binLocation;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }

        public BinLocationDTO getBinLocation() { return binLocation; }
        public void setBinLocation(BinLocationDTO binLocation) { this.binLocation = binLocation; }
    }

    /**
     * Inner DTO for BinLocation to avoid circular references
     */
    public static class BinLocationDTO {
        private Long id;
        private String binCode;
        private Integer row;
        private Integer column;

        public BinLocationDTO() {}

        public BinLocationDTO(Long id, String binCode, Integer row, Integer column) {
            this.id = id;
            this.binCode = binCode;
            this.row = row;
            this.column = column;
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
    }
}
