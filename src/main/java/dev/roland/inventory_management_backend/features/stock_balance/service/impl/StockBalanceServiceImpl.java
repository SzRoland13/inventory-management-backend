package dev.roland.inventory_management_backend.service.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;
import dev.roland.inventory_management_backend.features.stock_balance.repository.StockBalanceRepository;
import dev.roland.inventory_management_backend.features.stock_balance.service.StockBalanceService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockBalanceServiceImpl implements StockBalanceService {
  private final StockBalanceRepository stockBalanceRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<StockBalance, Long> getRepository() {
    return stockBalanceRepository;
  }

  /** {@inheritDoc} */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.STOCK_BALANCE;
  }
}
