package com.gridstock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for GridStock - Smart Warehouse & Picker Routing System
 * 
 * This is the entry point for our inventory management system that solves the physical
 * logistics problem of efficiently navigating warehouse aisles to pick items for orders.
 * We use Dijkstra's algorithm to calculate optimal picking routes through the warehouse.
 */
@SpringBootApplication
public class GridStockApplication {
    public static void main(String[] args) {
        SpringApplication.run(GridStockApplication.class, args);
    }
}
