package com.gridstock.repository;

import com.gridstock.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Order entity.
 * Provides CRUD operations and custom queries for order data.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Find order by order number
    Order findByOrderNumber(String orderNumber);
    
    // Find all orders with a specific status
    List<Order> findByStatus(String status);
    
    // Find pending orders (orders that need to be picked)
    default List<Order> findPendingOrders() {
        return findByStatus("PENDING");
    }
}
