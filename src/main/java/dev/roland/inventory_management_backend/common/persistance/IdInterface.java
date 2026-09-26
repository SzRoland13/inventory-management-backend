package dev.roland.inventory_management_backend.common.persistance;

import java.io.Serializable;

/**
 * Defines the common identifier accessor implemented by persisted domain models.
 *
 * @param <IdentifierT> serializable type of the model identifier
 */
public interface IdInterface<IdentifierT extends Serializable> {
  /**
   * Returns the entity identifier.
   *
   * @return identifier of this entity
   */
  IdentifierT getId();
}
