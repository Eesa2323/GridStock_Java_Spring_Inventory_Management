package com.gridstock.dsa;

import java.util.*;

/**
 * Graph data structure implemented using an adjacency list.
 * 
 * This graph represents the warehouse floor layout where:
 * - Each node represents a position in the warehouse (intersection or bin location)
 * - Each edge represents a walkable path between positions
 * - Edge weights represent the distance (or time) to walk between positions
 * 
 * We use an adjacency list representation because:
 * 1. It's memory-efficient for sparse graphs (most warehouses are sparse)
 * 2. It allows O(1) access to neighbors of a node
 * 3. It's perfect for Dijkstra's algorithm which needs to iterate through neighbors
 */
public class Graph {
    // Adjacency list: maps each node to its list of neighboring edges
    private Map<Integer, List<Edge>> adjacencyList;
    
    // Number of nodes in the graph
    private int numNodes;

    /**
     * Inner class representing an edge in the graph.
     * An edge connects two nodes with a weight (distance).
     */
    public static class Edge {
        int target;      // The destination node
        double weight;   // The distance/cost to reach this node
        
        public Edge(int target, double weight) {
            this.target = target;
            this.weight = weight;
        }
        
        public int getTarget() { return target; }
        public double getWeight() { return weight; }
    }

    /**
     * Constructor to create a graph with a specific number of nodes.
     * @param numNodes the total number of nodes in the graph
     */
    public Graph(int numNodes) {
        this.numNodes = numNodes;
        this.adjacencyList = new HashMap<>();
        
        // Initialize adjacency list for all nodes
        for (int i = 0; i < numNodes; i++) {
            adjacencyList.put(i, new ArrayList<>());
        }
    }

    /**
     * Add an undirected edge between two nodes.
     * Since warehouse aisles can be walked in both directions, we add edges in both directions.
     * 
     * @param from the starting node
     * @param to the ending node
     * @param weight the distance/cost between the nodes
     */
    public void addEdge(int from, int to, double weight) {
        // Add edge in both directions (undirected graph)
        adjacencyList.get(from).add(new Edge(to, weight));
        adjacencyList.get(to).add(new Edge(from, weight));
    }

    /**
     * Get all neighbors of a specific node.
     * This is used by Dijkstra's algorithm to explore possible next steps.
     * 
     * @param node the node whose neighbors we want
     * @return list of edges to neighboring nodes
     */
    public List<Edge> getNeighbors(int node) {
        return adjacencyList.getOrDefault(node, new ArrayList<>());
    }

    /**
     * Get the total number of nodes in the graph.
     */
    public int getNumNodes() {
        return numNodes;
    }

    /**
     * Convert a 2D grid coordinate (row, column) to a single node ID.
     * This is useful for mapping warehouse bin locations to graph nodes.
     * 
     * @param row the row coordinate
     * @param col the column coordinate
     * @param numColumns the total number of columns in the grid
     * @return the unique node ID
     */
    public static int coordinateToNodeId(int row, int col, int numColumns) {
        return row * numColumns + col;
    }

    /**
     * Convert a node ID back to 2D grid coordinates.
     * This is useful for visualizing the path on the warehouse grid.
     * 
     * @param nodeId the unique node ID
     * @param numColumns the total number of columns in the grid
     * @return an array [row, column]
     */
    public static int[] nodeIdToCoordinate(int nodeId, int numColumns) {
        int row = nodeId / numColumns;
        int col = nodeId % numColumns;
        return new int[]{row, col};
    }
}
