package com.gridstock.controller;

import com.gridstock.dto.OrderDTO;
import com.gridstock.entity.Order;
import com.gridstock.entity.OrderItem;
import com.gridstock.entity.Inventory;
import com.gridstock.repository.OrderRepository;
import com.gridstock.repository.OrderItemRepository;
import com.gridstock.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing orders.
 * Provides endpoints for CRUD operations on orders and order items.
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * Get all orders.
     * GET /api/orders
     */
    @GetMapping
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
            .map(this::convertToDTO)
            .toList();
    }

    /**
     * Get pending orders (orders that need to be picked).
     * GET /api/orders/pending
     */
    @GetMapping("/pending")
    public List<OrderDTO> getPendingOrders() {
        return orderRepository.findPendingOrders().stream()
            .map(this::convertToDTO)
            .toList();
    }

    /**
     * Get a specific order by ID.
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
            .map(this::convertToDTO)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get order by order number.
     * GET /api/orders/number/{orderNumber}
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Order> getOrderByNumber(@PathVariable String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber);
        return order != null ? ResponseEntity.ok(order) : ResponseEntity.notFound().build();
    }

    /**
     * Create a new order.
     * POST /api/orders
     */
    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        return orderRepository.save(order);
    }

    /**
     * Add an item to an existing order.
     * POST /api/orders/{orderId}/items
     * 
     * Accepts { "inventoryId": 1, "quantity": 2 }
     */
    @PostMapping("/{orderId}/items")
    public ResponseEntity<?> addOrderItem(@PathVariable Long orderId, @RequestBody OrderItemRequest request) {
        if (!orderRepository.existsById(orderId)) {
            return ResponseEntity.notFound().build();
        }
        Inventory inventory = inventoryRepository.findById(request.getInventoryId())
            .orElse(null);
        if (inventory == null) {
            return ResponseEntity.badRequest().body("Inventory item not found");
        }
        if (request.getQuantity() == null || request.getQuantity() < 1) {
            return ResponseEntity.badRequest().body("Quantity must be at least 1");
        }
        // Use getReferenceById so we get a valid FK reference without loading
        // the Order (and without touching its lazy orderItems collection,
        // which would fail once the Hibernate session is closed).
        Order order = orderRepository.getReferenceById(orderId);
        OrderItem orderItem = new OrderItem(order, inventory, request.getQuantity());
        orderItem.setQuantityPicked(0);
        orderItem.setIsPicked(false);
        OrderItem saved = orderItemRepository.save(orderItem);
        return ResponseEntity.ok(saved);
    }

    /**
     * Request body for adding an item to an order.
     */
    public static class OrderItemRequest {
        private Long inventoryId;
        private Integer quantity;

        public OrderItemRequest() {}

        public Long getInventoryId() { return inventoryId; }
        public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    /**
     * Update an existing order.
     * PUT /api/orders/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order order) {
        return orderRepository.findById(id)
            .map(existingOrder -> {
                existingOrder.setOrderNumber(order.getOrderNumber());
                existingOrder.setCustomerName(order.getCustomerName());
                existingOrder.setStatus(order.getStatus());
                return ResponseEntity.ok(orderRepository.save(existingOrder));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update order status (e.g., from PENDING to PICKING to COMPLETED).
     * PATCH /api/orders/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        return orderRepository.findById(id)
            .map(order -> {
                order.setStatus(status);
                return ResponseEntity.ok(orderRepository.save(order));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Pick an item from an order.
     * POST /api/orders/{orderId}/items/{itemId}/pick
     */
    @PostMapping("/{orderId}/items/{itemId}/pick")
    public ResponseEntity<?> pickOrderItem(@PathVariable Long orderId, @PathVariable Long itemId, @RequestParam Integer quantity) {
        return orderRepository.findById(orderId)
            .map(order -> {
                OrderItem orderItem = orderItemRepository.findById(itemId).orElse(null);
                if (orderItem == null || !orderItem.getOrder().getId().equals(orderId)) {
                    return ResponseEntity.badRequest().body("Order item not found");
                }

                // Check if quantity to pick is valid
                if (quantity <= 0 || quantity > (orderItem.getQuantity() - orderItem.getQuantityPicked())) {
                    return ResponseEntity.badRequest().body("Invalid quantity to pick");
                }

                // Update order item
                orderItem.pickQuantity(quantity);
                orderItemRepository.save(orderItem);

                // Deduct from inventory
                com.gridstock.entity.Inventory inventory = orderItem.getInventory();
                inventory.setQuantity(inventory.getQuantity() - quantity);
                inventoryRepository.save(inventory);

                // Check if all items are picked (fetch items explicitly to avoid lazy loading)
                boolean allPicked = orderItemRepository.findByOrderId(orderId).stream().allMatch(OrderItem::isFullyPicked);
                if (allPicked) {
                    order.setStatus("READY_TO_SHIP");
                    orderRepository.save(order);
                }

                return ResponseEntity.ok(convertToDTO(order));
            })
            .orElse(ResponseEntity.notFound().build());
    }


    /**
     * Delete an order.
     * DELETE /api/orders/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Helper method to convert Order entity to OrderDTO.
     * This explicitly fetches the order items to avoid lazy loading issues.
     */
    private OrderDTO convertToDTO(Order order) {
        List<com.gridstock.entity.OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        
        List<OrderDTO.OrderItemDTO> itemDTOs = items.stream().map(item -> {
            OrderDTO.InventoryDTO invDTO = new OrderDTO.InventoryDTO(
                item.getInventory().getId(),
                item.getInventory().getSku(),
                item.getInventory().getProductName(),
                item.getInventory().getQuantity(),
                item.getInventory().getPrice(),
                new OrderDTO.BinLocationDTO(
                    item.getInventory().getBinLocation().getId(),
                    item.getInventory().getBinLocation().getBinCode(),
                    item.getInventory().getBinLocation().getRow(),
                    item.getInventory().getBinLocation().getColumn()
                )
            );
            return new OrderDTO.OrderItemDTO(
                item.getId(),
                item.getQuantity(),
                item.getQuantityPicked(),
                item.getIsPicked(),
                invDTO
            );
        }).toList();
        
        return new OrderDTO(
            order.getId(),
            order.getOrderNumber(),
            order.getCustomerName(),
            order.getStatus(),
            order.getCreatedAt(),
            order.getCompletedAt(),
            itemDTOs
        );
    }
}
