package com.gridstock.dsa;

import java.util.*;

/**
 * Dijkstra's Algorithm implementation for finding shortest paths in a weighted graph.
 * 
 * This is the heart of our path optimization system. When a worker needs to pick items
 * from multiple bin locations, we use Dijkstra's algorithm to find the optimal route
 * that minimizes total walking distance.
 * 
 * Algorithm Overview:
 * 1. Start from the entrance node (usually node 0)
 * 2. Maintain a priority queue of nodes to visit, ordered by distance from start
 * 3. For each node, relax all its edges (update distances to neighbors if shorter path found)
 * 4. Continue until all target nodes are visited
 * 5. Reconstruct the shortest path from the distance map
 * 
 * Time Complexity: O((V + E) log V) using a priority queue
 * Space Complexity: O(V) for the distance and predecessor maps
 */
public class DijkstraAlgorithm {

    /**
     * Find the shortest path from a start node to an end node in the graph.
     * 
     * @param graph the warehouse graph
     * @param startNode the starting position (e.g., warehouse entrance)
     * @param endNode the target position (e.g., bin location)
     * @return a list of node IDs representing the shortest path, or empty list if no path exists
     */
    public static List<Integer> findShortestPath(Graph graph, int startNode, int endNode) {
        // Distance map: stores the shortest known distance to each node
        Map<Integer, Double> distances = new HashMap<>();
        
        // Predecessor map: stores the previous node in the shortest path (for reconstruction)
        Map<Integer, Integer> predecessors = new HashMap<>();
        
        // Priority queue: always processes the node with the smallest known distance
        // Using a PriorityQueue with a custom comparator to sort by distance
        PriorityQueue<NodeDistance> priorityQueue = new PriorityQueue<>(
            Comparator.comparingDouble(NodeDistance::getDistance)
        );
        
        // Initialize all distances to infinity (representing "unknown")
        for (int i = 0; i < graph.getNumNodes(); i++) {
            distances.put(i, Double.MAX_VALUE);
        }
        
        // Distance to start node is 0
        distances.put(startNode, 0.0);
        priorityQueue.add(new NodeDistance(startNode, 0.0));
        
        // Main Dijkstra loop
        while (!priorityQueue.isEmpty()) {
            // Get the node with the smallest distance
            NodeDistance current = priorityQueue.poll();
            int currentNode = current.getNode();
            double currentDistance = current.getDistance();
            
            // If we've reached the target node, we can stop early
            if (currentNode == endNode) {
                break;
            }
            
            // Skip if we've already found a better path to this node
            if (currentDistance > distances.get(currentNode)) {
                continue;
            }
            
            // Explore all neighbors of the current node
            for (Graph.Edge edge : graph.getNeighbors(currentNode)) {
                int neighbor = edge.getTarget();
                double edgeWeight = edge.getWeight();
                double newDistance = currentDistance + edgeWeight;
                
                // If we found a shorter path to the neighbor, update it
                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    predecessors.put(neighbor, currentNode);
                    priorityQueue.add(new NodeDistance(neighbor, newDistance));
                }
            }
        }
        
        // Reconstruct the path from start to end using predecessors
        return reconstructPath(predecessors, startNode, endNode);
    }

    /**
     * Find the optimal route to visit multiple target nodes (bin locations).
     * This solves the Traveling Salesman Problem variant for order picking.
     * We use a greedy approach: always go to the nearest unvisited target.
     * 
     * @param graph the warehouse graph
     * @param startNode the starting position (warehouse entrance)
     * @param targetNodes the list of bin locations that need to be visited
     * @return an ordered list of node IDs representing the optimal picking route
     */
    public static List<Integer> findOptimalRoute(Graph graph, int startNode, List<Integer> targetNodes) {
        List<Integer> route = new ArrayList<>();
        Set<Integer> unvisited = new HashSet<>(targetNodes);
        int currentNode = startNode;
        
        // Greedy approach: always go to the nearest unvisited target
        while (!unvisited.isEmpty()) {
            int nearestTarget = findNearestTarget(graph, currentNode, unvisited);
            
            // Find the shortest path to the nearest target
            List<Integer> pathToTarget = findShortestPath(graph, currentNode, nearestTarget);
            
            // Add the path to our route (excluding the first node to avoid duplicates)
            if (route.isEmpty()) {
                route.addAll(pathToTarget);
            } else {
                // Skip the first node since it's the same as the last node in our current route
                route.addAll(pathToTarget.subList(1, pathToTarget.size()));
            }
            
            // Mark this target as visited
            unvisited.remove(nearestTarget);
            currentNode = nearestTarget;
        }
        
        return route;
    }

    /**
     * Find the nearest unvisited target node from the current position.
     * This is a helper for the optimal route calculation.
     */
    private static int findNearestTarget(Graph graph, int currentNode, Set<Integer> unvisited) {
        int nearest = -1;
        double minDistance = Double.MAX_VALUE;
        
        for (int target : unvisited) {
            // Run Dijkstra to find distance to this target
            List<Integer> path = findShortestPath(graph, currentNode, target);
            double distance = calculatePathDistance(graph, path);
            
            if (distance < minDistance) {
                minDistance = distance;
                nearest = target;
            }
        }
        
        return nearest;
    }

    /**
     * Calculate the total distance of a path by summing edge weights.
     */
    private static double calculatePathDistance(Graph graph, List<Integer> path) {
        if (path.size() < 2) return 0.0;
        
        double totalDistance = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            int from = path.get(i);
            int to = path.get(i + 1);
            
            // Find the edge weight between these nodes
            for (Graph.Edge edge : graph.getNeighbors(from)) {
                if (edge.getTarget() == to) {
                    totalDistance += edge.getWeight();
                    break;
                }
            }
        }
        
        return totalDistance;
    }

    /**
     * Reconstruct the path from the predecessor map.
     * This traces back from the end node to the start node using the predecessors.
     */
    private static List<Integer> reconstructPath(Map<Integer, Integer> predecessors, int startNode, int endNode) {
        List<Integer> path = new ArrayList<>();
        Integer current = endNode;
        
        // Trace back from end to start
        while (current != null && !current.equals(startNode)) {
            path.add(current);
            current = predecessors.get(current);
            
            // If we can't reach the start node, no path exists
            if (current == null) {
                return new ArrayList<>();
            }
        }
        
        // Add the start node
        path.add(startNode);
        
        // Reverse to get the path from start to end
        Collections.reverse(path);
        
        return path;
    }

    /**
     * Helper class to store node-distance pairs for the priority queue.
     */
    private static class NodeDistance {
        private int node;
        private double distance;
        
        public NodeDistance(int node, double distance) {
            this.node = node;
            this.distance = distance;
        }
        
        public int getNode() { return node; }
        public double getDistance() { return distance; }
    }
}
