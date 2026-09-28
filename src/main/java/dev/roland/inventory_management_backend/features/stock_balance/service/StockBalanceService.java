package dev.roland.inventory_management_backend.features.stock_balance.service;

import java.util.List;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;

/** Defines operations supported by the stock balance feature. */
public interface StockBalanceService extends BaseService<StockBalance, Long> {
  /**
   * Finds matching records using the supplied criteria.
   *
   * @param productId the product identifier
   * @return the matching resources
   */
  List<StockBalance> findAllByProductId(Long productId);

  /**
   * Finds matching records using the supplied criteria.
   *
   * @param productIds the product identifiers
   * @return the matching resources
   */
  List<StockBalance> findAllByProductIdIn(List<Long> productIds);
}
