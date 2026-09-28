package dev.roland.inventory_management_backend.features.brand.message;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Validation message keys used by brand request constraints. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BrandValidationMessage {
  public static final String REQUIRED = "brand.validation.required";
  public static final String TOO_LONG = "brand.validation.too_long";
}
