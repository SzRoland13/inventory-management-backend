package dev.roland.inventory_management_backend.common.message;

/** Provides stable keys used to resolve localized API messages. */
public interface MessageKey {
  /**
   * Returns the stable localization key for this message.
   *
   * @return localization key
   */
  String getKey();
}
