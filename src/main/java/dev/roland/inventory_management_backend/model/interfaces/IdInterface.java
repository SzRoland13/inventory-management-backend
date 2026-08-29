package dev.roland.inventory_management_backend.model.interfaces;

import java.io.Serializable;

/**
 * Defines the common identifier accessor implemented by persisted domain models.
 *
 * @param <ID> serializable type of the model identifier
 */
public interface IdInterface<ID extends Serializable> {
  ID getId();
}
