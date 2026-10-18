package com.gridstock.repository;

import com.gridstock.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Warehouse entity.
 * Provides CRUD operations and custom queries for warehouse data.
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
}
