package dev.roland.inventory_management_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.model.Product;
import dev.roland.inventory_management_backend.model.StockBalance;
import dev.roland.inventory_management_backend.model.Warehouse;

public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {
  Optional<StockBalance> findByWarehouseAndProduct(Warehouse warehouse, Product product);
}
