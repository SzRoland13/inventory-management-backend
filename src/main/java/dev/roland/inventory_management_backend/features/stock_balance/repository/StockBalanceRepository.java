package dev.roland.inventory_management_backend.features.stock_balance.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;

public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {
  Optional<StockBalance> findByWarehouseAndProduct(Warehouse warehouse, Product product);
}
