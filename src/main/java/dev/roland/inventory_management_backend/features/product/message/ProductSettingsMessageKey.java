package dev.roland.inventory_management_backend.features.product.message;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Success message keys for product settings operations. */
@Getter
@RequiredArgsConstructor
public enum ProductSettingsMessageKey implements MessageKey {
  UNITS_RETRIEVED("product.settings.units.retrieved"),
  UNIT_CREATED("product.settings.unit.created"),
  UNIT_UPDATED("product.settings.unit.updated"),
  UNIT_DELETED("product.settings.unit.deleted"),
  CATEGORIES_RETRIEVED("product.settings.categories.retrieved"),
  CATEGORY_CREATED("product.settings.category.created"),
  CATEGORY_UPDATED("product.settings.category.updated"),
  CATEGORY_DELETED("product.settings.category.deleted"),
  ATTRIBUTES_RETRIEVED("product.settings.attributes.retrieved"),
  ATTRIBUTE_CREATED("product.settings.attribute.created"),
  ATTRIBUTE_UPDATED("product.settings.attribute.updated"),
  ATTRIBUTE_DELETED("product.settings.attribute.deleted"),
  ATTRIBUTE_OPTION_CREATED("product.settings.attribute_option.created"),
  ATTRIBUTE_OPTION_UPDATED("product.settings.attribute_option.updated"),
  ATTRIBUTE_OPTION_DELETED("product.settings.attribute_option.deleted");

  private final String key;
}
