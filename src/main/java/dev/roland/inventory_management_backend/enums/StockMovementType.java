package dev.roland.inventory_management_backend.enums;

public enum StockMovementType {
  /** Represents the purchase inbound value. */
  PURCHASE_INBOUND,
  /** Represents the sale outbound value. */
  SALE_OUTBOUND,
  /** Represents the transfer out value. */
  TRANSFER_OUT,
  /** Represents the transfer in value. */
  TRANSFER_IN,
  /** Represents the adjustment increase value. */
  ADJUSTMENT_INCREASE,
  /** Represents the adjustment decrease value. */
  ADJUSTMENT_DECREASE,
  /** Represents the return in value. */
  RETURN_IN,
  /** Represents the return out value. */
  RETURN_OUT
}
