package dev.roland.inventory_management_backend.features.document.dto;

import static dev.roland.inventory_management_backend.features.document.dto.DocumentApiDtoCollections.immutableList;

import java.util.List;

import dev.roland.inventory_management_backend.features.document.enumeration.DocumentQueueView;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;

/**
 * Defines the capabilities document API contract.
 *
 * @param availableQueueViews available queue views value
 * @param creatableTypes creatable types value
 */
public record Capabilities(
    List<DocumentType> creatableTypes, List<DocumentQueueView> availableQueueViews) {
  /**
   * Copies capability collections to prevent external mutation.
   *
   * @param creatableTypes types the user can create
   * @param availableQueueViews queue views available to the user
   */
  public Capabilities {
    creatableTypes = immutableList(creatableTypes);
    availableQueueViews = immutableList(availableQueueViews);
  }

  /**
   * Returns a defensive copy of creatable document types.
   *
   * @return immutable creatable types
   */
  @Override
  public List<DocumentType> creatableTypes() {
    return immutableList(creatableTypes);
  }

  /**
   * Returns a defensive copy of available queue views.
   *
   * @return immutable queue views
   */
  @Override
  public List<DocumentQueueView> availableQueueViews() {
    return immutableList(availableQueueViews);
  }
}
