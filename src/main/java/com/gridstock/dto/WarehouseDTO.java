package com.gridstock.dto;

import java.util.List;

/**
 * DTO for Warehouse with bin locations included.
 * This solves the lazy loading issue by explicitly fetching the needed data.
 */
public class WarehouseDTO {
    private Long id;
    private String name;
    private Integer rows;
    private Integer columns;
    private String location;
    private List<BinLocationDTO> binLocations;

    public WarehouseDTO() {}

    public WarehouseDTO(Long id, String name, Integer rows, Integer columns, String location, List<BinLocationDTO> binLocations) {
        this.id = id;
        this.name = name;
        this.rows = rows;
        this.columns = columns;
        this.location = location;
        this.binLocations = binLocations;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getRows() { return rows; }
    public void setRows(Integer rows) { this.rows = rows; }

    public Integer getColumns() { return columns; }
    public void setColumns(Integer columns) { this.columns = columns; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public List<BinLocationDTO> getBinLocations() { return binLocations; }
    public void setBinLocations(List<BinLocationDTO> binLocations) { this.binLocations = binLocations; }

    /**
     * Inner DTO for BinLocation to avoid circular references
     */
    public static class BinLocationDTO {
        private Long id;
        private String binCode;
        private Integer row;
        private Integer column;
        private Boolean hasInventory;

        public BinLocationDTO() {}

        public BinLocationDTO(Long id, String binCode, Integer row, Integer column, Boolean hasInventory) {
            this.id = id;
            this.binCode = binCode;
            this.row = row;
            this.column = column;
            this.hasInventory = hasInventory;
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

        public Boolean getHasInventory() { return hasInventory; }
        public void setHasInventory(Boolean hasInventory) { this.hasInventory = hasInventory; }
    }
}
