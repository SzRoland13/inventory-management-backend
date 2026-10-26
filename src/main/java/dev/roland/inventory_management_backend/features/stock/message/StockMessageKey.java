package dev.roland.inventory_management_backend.features.stock.message;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Message keys for read-only stock endpoints. */
@Getter
@RequiredArgsConstructor
public enum StockMessageKey implements MessageKey {
  BALANCES_RETRIEVED("stock.balances.retrieved"),
  MOVEMENTS_RETRIEVED("stock.movements.retrieved");

  private final String key;
}
