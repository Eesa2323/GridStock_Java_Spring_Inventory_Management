package com.gridstock.repository;

import com.gridstock.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for OrderItem entity.
 * Provides CRUD operations and custom queries for order item data.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Find all items in a specific order
    java.util.List<OrderItem> findByOrderId(Long orderId);
}
