package com.gridstock.controller;

import com.gridstock.entity.Order;
import com.gridstock.repository.OrderRepository;
import com.gridstock.service.PathOptimizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for path optimization.
 * Provides endpoints to calculate optimal picking routes using Dijkstra's algorithm.
 * This is the core feature that makes GridStock stand out from basic inventory systems.
 */
@RestController
@RequestMapping("/api/path-optimization")
@CrossOrigin(origins = "*")
public class PathOptimizationController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PathOptimizationService pathOptimizationService;

    /**
     * Calculate the optimal picking route for an order.
     * This endpoint uses Dijkstra's algorithm to find the shortest path through
     * all bin locations in the order, minimizing worker travel time.
     * 
     * GET /api/path-optimization/route/{orderId}
     * 
     * @param orderId the ID of the order to optimize
     * @return the optimized route with step-by-step instructions
     */
    @GetMapping("/route/{orderId}")
    public ResponseEntity<?> calculateOptimalRoute(@PathVariable Long orderId) {
        return orderRepository.findById(orderId)
            .map(order -> {
                try {
                    PathOptimizationService.OptimizedRoute route = pathOptimizationService.calculateOptimalRoute(order);
                    return ResponseEntity.ok(route);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body("Error calculating route: " + e.getMessage());
                }
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Calculate the optimal picking route by order number.
     * Alternative endpoint that uses order number instead of ID.
     * 
     * GET /api/path-optimization/route/by-number/{orderNumber}
     * 
     * @param orderNumber the order number to optimize
     * @return the optimized route with step-by-step instructions
     */
    @GetMapping("/route/by-number/{orderNumber}")
    public ResponseEntity<?> calculateOptimalRouteByNumber(@PathVariable String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            PathOptimizationService.OptimizedRoute route = pathOptimizationService.calculateOptimalRoute(order);
            return ResponseEntity.ok(route);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error calculating route: " + e.getMessage());
        }
    }
}
