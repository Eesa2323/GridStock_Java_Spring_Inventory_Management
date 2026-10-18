package com.gridstock.config;

import com.gridstock.entity.*;
import com.gridstock.repository.BinLocationRepository;
import com.gridstock.repository.InventoryRepository;
import com.gridstock.repository.OrderItemRepository;
import com.gridstock.repository.OrderRepository;
import com.gridstock.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Data seeder to populate the database with sample data.
 * 
 *  creates a  warehouse scenario with:
 * - A 5x5 warehouse grid (25 bin locations)
 * - Sample inventory items stored in various bin locations
 * - Sample orders that need to be picked
 * 
 *  allows immediate testing of the path optimization feature.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private BinLocationRepository binLocationRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only seed if the database is empty
        if (warehouseRepository.count() > 0) {
            return;
        }

        System.out.println("Seeding database with sample data...");

        // Create a warehouse (5x5 grid)
        Warehouse warehouse = new Warehouse("Main Warehouse", 5, 5, "123 Industrial Blvd");
        warehouse = warehouseRepository.save(warehouse);

        // Create bin locations in a 5x5 grid
        List<BinLocation> binLocations = new ArrayList<>();
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                String binCode = String.format("A-%02d-%02d", row + 1, col + 1);
                BinLocation bin = new BinLocation(binCode, row, col, warehouse);
                bin = binLocationRepository.save(bin);
                binLocations.add(bin);
            }
        }

        // Create inventory items at various bin locations
        //  place items at specific locations to test path optimization
        Inventory[] inventoryItems = {
            createInventory("SKU-001", "Wireless Mouse", 50, 10, 29.99, binLocations.get(6)),   // Row 1, Col 1
            createInventory("SKU-002", "Mechanical Keyboard", 30, 5, 89.99, binLocations.get(12)), // Row 2, Col 2
            createInventory("SKU-003", "27-inch Monitor", 15, 3, 299.99, binLocations.get(18)), // Row 3, Col 3
            createInventory("SKU-004", "USB-C Hub", 40, 8, 49.99, binLocations.get(8)),        // Row 1, Col 3
            createInventory("SKU-005", "Webcam HD", 25, 5, 79.99, binLocations.get(20)),       // Row 4, Col 0
            createInventory("SKU-006", "Laptop Stand", 35, 7, 39.99, binLocations.get(2)),     // Row 0, Col 2
            createInventory("SKU-007", "Noise Cancelling Headphones", 20, 4, 199.99, binLocations.get(14)), // Row 2, Col 4
            createInventory("SKU-008", "External SSD 1TB", 45, 10, 129.99, binLocations.get(10)), // Row 2, Col 0
            createInventory("SKU-009", "Wireless Charger", 60, 15, 34.99, binLocations.get(16)), // Row 3, Col 1
            createInventory("SKU-010", "Bluetooth Speaker", 28, 6, 59.99, binLocations.get(22)), // Row 4, Col 2
        };

        // Save all inventory items
        for (Inventory inv : inventoryItems) {
            inventoryRepository.save(inv);
        }

        // Create sample orders
        // Order 1: Items scattered across the warehouse (good for testing path optimization)
        Order order1 = new Order("ORD-2024-001", "John Smith");
        order1 = orderRepository.save(order1);
        
        // Add items to order 1 (items at different locations: SKU-001, SKU-003, SKU-005)
        OrderItem item1 = new OrderItem(order1, inventoryItems[0], 2);  // Mouse
        item1.setQuantityPicked(0);
        item1.setIsPicked(false);
        orderItemRepository.save(item1);
        order1.addOrderItem(item1);
        
        OrderItem item2 = new OrderItem(order1, inventoryItems[2], 1);  // Monitor
        item2.setQuantityPicked(0);
        item2.setIsPicked(false);
        orderItemRepository.save(item2);
        order1.addOrderItem(item2);
        
        OrderItem item3 = new OrderItem(order1, inventoryItems[4], 1);  // Webcam
        item3.setQuantityPicked(0);
        item3.setIsPicked(false);
        orderItemRepository.save(item3);
        order1.addOrderItem(item3);
        orderRepository.save(order1);

        // Order 2: Clustered items (easier route)
        Order order2 = new Order("ORD-2024-002", "Jane Doe");
        order2 = orderRepository.save(order2);
        
        // Add items to order 2 (items closer together: SKU-002, SKU-004, SKU-007)
        OrderItem item4 = new OrderItem(order2, inventoryItems[1], 1);  // Keyboard
        item4.setQuantityPicked(0);
        item4.setIsPicked(false);
        orderItemRepository.save(item4);
        order2.addOrderItem(item4);
        
        OrderItem item5 = new OrderItem(order2, inventoryItems[3], 3);  // USB Hub
        item5.setQuantityPicked(0);
        item5.setIsPicked(false);
        orderItemRepository.save(item5);
        order2.addOrderItem(item5);
        
        OrderItem item6 = new OrderItem(order2, inventoryItems[6], 1);  // Headphones
        item6.setQuantityPicked(0);
        item6.setIsPicked(false);
        orderItemRepository.save(item6);
        order2.addOrderItem(item6);
        orderRepository.save(order2);

        // Order 3: Single item (simplest case)
        Order order3 = new Order("ORD-2024-003", "Bob Johnson");
        order3 = orderRepository.save(order3);
        
        OrderItem item7 = new OrderItem(order3, inventoryItems[7], 2);  // SSD
        item7.setQuantityPicked(0);
        item7.setIsPicked(false);
        orderItemRepository.save(item7);
        order3.addOrderItem(item7);
        orderRepository.save(order3);

        System.out.println("Database seeding completed!");
        System.out.println("Created warehouse: " + warehouse.getName() + " (" + warehouse.getRows() + "x" + warehouse.getColumns() + ")");
        System.out.println("Created " + binLocations.size() + " bin locations");
        System.out.println("Created " + inventoryItems.length + " inventory items");
        System.out.println("Created 3 sample orders for testing path optimization");
    }

    /**
     * Helper method to create an inventory item.
     */
    private Inventory createInventory(String sku, String productName, int quantity, int minThreshold, double price, BinLocation binLocation) {
        Inventory inventory = new Inventory(sku, productName, quantity, minThreshold, price);
        inventory.setBinLocation(binLocation);
        binLocation.setInventory(inventory);
        return inventory;
    }
}
