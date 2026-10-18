package com.gridstock.repository;

import com.gridstock.entity.BinLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for BinLocation entity.
 * Provides CRUD operations and custom queries for bin location data.
 */
@Repository
public interface BinLocationRepository extends JpaRepository<BinLocation, Long> {
    // Find bin location by its unique code
    BinLocation findByBinCode(String binCode);
    
    // Find all bin locations in a specific warehouse
    java.util.List<BinLocation> findByWarehouseId(Long warehouseId);
}
