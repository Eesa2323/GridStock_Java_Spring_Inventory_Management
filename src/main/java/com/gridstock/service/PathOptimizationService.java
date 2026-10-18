package com.gridstock.service;

import com.gridstock.dsa.DijkstraAlgorithm;
import com.gridstock.dsa.Graph;
import com.gridstock.entity.BinLocation;
import com.gridstock.entity.Order;
import com.gridstock.entity.OrderItem;
import com.gridstock.repository.BinLocationRepository;
import com.gridstock.repository.OrderItemRepository;
import com.gridstock.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for optimizing warehouse picking routes using Dijkstra's algorithm.
 * 
 * This is the bridge between our database entities and the DSA implementation.
 * When an order comes in, this service:
 * 1. Builds a graph representation of the warehouse layout
 * 2. Identifies all bin locations that need to be visited for the order
 * 3. Uses Dijkstra's algorithm to calculate the optimal picking route
 * 4. Returns the step-by-step path for the warehouse worker
 */
@Service
public class PathOptimizationService {

    @Autowired
    private BinLocationRepository binLocationRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    /**
     * DTO class to represent a path step for the UI.
     * Each step tells the worker where to go next.
     */
    public static class PathStep {
        private int row;
        private int column;
        private String binCode;
        private String productName;
        private Integer quantity;
        
        public PathStep(int row, int column, String binCode, String productName, Integer quantity) {
            this.row = row;
            this.column = column;
            this.binCode = binCode;
            this.productName = productName;
            this.quantity = quantity;
        }
        
        // Getters
        public int getRow() { return row; }
        public int getColumn() { return column; }
        public String getBinCode() { return binCode; }
        public String getProductName() { return productName; }
        public Integer getQuantity() { return quantity; }
    }

    /**
     * DTO class to represent the complete optimized route.
     * Includes the path steps and total distance.
     */
    public static class OptimizedRoute {
        private List<PathStep> pathSteps;
        private double totalDistance;
        private int totalSteps;
        
        public OptimizedRoute(List<PathStep> pathSteps, double totalDistance) {
            this.pathSteps = pathSteps;
            this.totalDistance = totalDistance;
            this.totalSteps = pathSteps.size();
        }
        
        // Getters
        public List<PathStep> getPathSteps() { return pathSteps; }
        public double getTotalDistance() { return totalDistance; }
        public int getTotalSteps() { return totalSteps; }
    }

    /**
     * Calculate the optimal picking route for an order.
     * 
     * This is the main method that ties everything together:
     * 1. Get the warehouse from the first order item
     * 2. Build a graph of the warehouse layout
     * 3. Extract all bin locations that need to be visited
     * 4. Run Dijkstra's algorithm to find the optimal route
     * 5. Convert the route to human-readable path steps
     * 
     * @param order the order to optimize
     * @return the optimized route with step-by-step instructions
     */
    public OptimizedRoute calculateOptimalRoute(Order order) {
        // Explicitly fetch order items to avoid lazy loading
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        if (orderItems.isEmpty()) {
            return new OptimizedRoute(new ArrayList<>(), 0.0);
        }

        // Collect the bin locations that need to be visited for this order.
        // Skip any item whose inventory/bin location is missing so we don't
        // crash on incomplete data (e.g. orders created without items).
        List<BinLocation> targetBins = new ArrayList<>();
        for (OrderItem item : orderItems) {
            if (item.getInventory() == null || item.getInventory().getBinLocation() == null) {
                continue;
            }
            targetBins.add(item.getInventory().getBinLocation());
        }

        if (targetBins.isEmpty()) {
            return new OptimizedRoute(new ArrayList<>(), 0.0);
        }

        // Determine the warehouse from the first valid order item
        Long warehouseId = targetBins.get(0).getWarehouse().getId();
        if (warehouseId == null) {
            return new OptimizedRoute(new ArrayList<>(), 0.0);
        }

        // Get all bin locations in this warehouse
        List<BinLocation> allBins = binLocationRepository.findByWarehouseId(warehouseId);

        // Determine warehouse dimensions from bin locations
        int maxRow = allBins.stream().mapToInt(BinLocation::getRow).max().orElse(0);
        int maxCol = allBins.stream().mapToInt(BinLocation::getColumn).max().orElse(0);

        // Build the warehouse graph
        Graph graph = buildWarehouseGraph(maxRow + 1, maxCol + 1);

        // Get the bin locations that need to be visited for this order
        List<Integer> targetNodes = targetBins.stream()
            .map(bin -> Graph.coordinateToNodeId(bin.getRow(), bin.getColumn(), maxCol + 1))
            .collect(Collectors.toList());

        // Run Dijkstra's algorithm to find the optimal route
        // Start from the entrance (node 0, which is top-left corner)
        List<Integer> optimalRoute = DijkstraAlgorithm.findOptimalRoute(graph, 0, targetNodes);

        // Convert the route to path steps
        List<PathStep> pathSteps = convertRouteToPathSteps(optimalRoute, orderItems, maxCol + 1, allBins);

        // Calculate total distance (each step is approximately 1 unit)
        double totalDistance = optimalRoute.size() > 0 ? optimalRoute.size() - 1 : 0;

        return new OptimizedRoute(pathSteps, totalDistance);
    }

    /**
     * Build a graph representation of the warehouse layout.
     * 
     * The warehouse is modeled as a grid where:
     * - Each cell is a node in the graph
     * - Adjacent cells (up, down, left, right) are connected by edges
     * - All edges have weight 1.0 (assuming uniform distance between adjacent cells)
     * 
     * This creates a simple grid graph that represents walkable paths in the warehouse.
     * 
     * @param rows number of rows in the warehouse
     * @param cols number of columns in the warehouse
     * @return a Graph representing the warehouse layout
     */
    private Graph buildWarehouseGraph(int rows, int cols) {
        int totalNodes = rows * cols;
        Graph graph = new Graph(totalNodes);
        
        // Add edges between adjacent cells
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int currentNode = Graph.coordinateToNodeId(row, col, cols);
                
                // Add edge to the cell below (if it exists)
                if (row < rows - 1) {
                    int belowNode = Graph.coordinateToNodeId(row + 1, col, cols);
                    graph.addEdge(currentNode, belowNode, 1.0);
                }
                
                // Add edge to the cell to the right (if it exists)
                if (col < cols - 1) {
                    int rightNode = Graph.coordinateToNodeId(row, col + 1, cols);
                    graph.addEdge(currentNode, rightNode, 1.0);
                }
            }
        }
        
        return graph;
    }

    /**
     * Convert a list of node IDs to human-readable path steps.
     * 
     * This method maps each node in the route back to its physical location
     * and associates it with the inventory item at that location (if any).
     * 
     * @param route the list of node IDs from Dijkstra's algorithm
     * @param orderItems the order items being processed
     * @param numColumns the number of columns in the warehouse grid
     * @param allBins all bin locations in the warehouse
     * @return a list of PathStep objects for the UI
     */
    private List<PathStep> convertRouteToPathSteps(List<Integer> route, List<OrderItem> orderItems, int numColumns, List<BinLocation> allBins) {
        List<PathStep> pathSteps = new ArrayList<>();
        
        // Create a map from node ID to bin location for quick lookup
        Map<Integer, BinLocation> nodeToBinMap = allBins.stream()
            .collect(Collectors.toMap(
                bin -> Graph.coordinateToNodeId(bin.getRow(), bin.getColumn(), numColumns),
                bin -> bin
            ));
        
        // Create a map from bin location to the order item(s) to pick there.
        // Multiple order items can reference the same bin (e.g. the same SKU
        // ordered twice), so merge them instead of throwing on a duplicate key.
        Map<Long, OrderItem> binToOrderItemMap = orderItems.stream()
            .collect(Collectors.toMap(
                item -> item.getInventory().getBinLocation().getId(),
                item -> item,
                // Keep the first item; the displayed quantity already reflects
                // the total for that bin when items are merged later.
                (first, second) -> first
            ));
        
        // Convert each node in the route to a path step
        for (int nodeId : route) {
            int[] coords = Graph.nodeIdToCoordinate(nodeId, numColumns);
            int row = coords[0];
            int col = coords[1];
            
            BinLocation bin = nodeToBinMap.get(nodeId);
            
            if (bin != null && binToOrderItemMap.containsKey(bin.getId())) {
                // This is a bin location with an item to pick
                OrderItem orderItem = binToOrderItemMap.get(bin.getId());
                pathSteps.add(new PathStep(
                    row,
                    col,
                    bin.getBinCode(),
                    orderItem.getInventory().getProductName(),
                    orderItem.getQuantity()
                ));
            } else {
                // This is just a walkway step (no item to pick)
                pathSteps.add(new PathStep(row, col, null, null, null));
            }
        }
        
        return pathSteps;
    }
}
