package dev.roland.inventory_management_backend.features.product.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import dev.roland.inventory_management_backend.features.brand.Brand;
import dev.roland.inventory_management_backend.features.brand.dto.BrandResponse;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.dto.ProductAttributeValueResponse;
import dev.roland.inventory_management_backend.features.product.dto.ProductRequest;
import dev.roland.inventory_management_backend.features.product.dto.ProductResponse;
import dev.roland.inventory_management_backend.features.product.dto.ProductStockResponse;
import dev.roland.inventory_management_backend.features.product_attribute_value.ProductAttributeValue;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;
import dev.roland.inventory_management_backend.features.unit.Unit;

/** Maps scalar product data to and from API representations. */
@Mapper(
    componentModel = "spring",
    uses = ProductSettingsMapper.class,
    unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProductMapper {

  /**
   * Updates product scalar fields from a request, leaving references and status to the facade.
   *
   * @param request product values to map
   * @param product target product to update
   */
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "sku", source = "sku")
  @Mapping(target = "ean", source = "ean")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "description", source = "description")
  @Mapping(target = "netPrice", source = "pricing.netPrice")
  @Mapping(target = "costPrice", source = "pricing.costPrice")
  @Mapping(target = "vatRate", source = "pricing.vatRate")
  @Mapping(target = "weight", source = "dimensions.weight")
  @Mapping(target = "width", source = "dimensions.width")
  @Mapping(target = "height", source = "dimensions.height")
  @Mapping(target = "depth", source = "dimensions.depth")
  void updateScalarFields(ProductRequest request, @MappingTarget Product product);

  /**
   * Maps an attribute value entity to its response without exposing JPA relationships.
   *
   * @param value attribute value to map
   * @return mapped attribute value response
   */
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "definitionId", source = "definition.id")
  @Mapping(target = "definitionCode", source = "definition.code")
  @Mapping(target = "definitionName", source = "definition.name")
  @Mapping(target = "valueType", source = "definition.valueType")
  @Mapping(target = "optionId", source = "option.id")
  @Mapping(target = "optionValue", source = "option.value")
  @Mapping(target = "textValue", source = "textValue")
  @Mapping(target = "numberValue", source = "numberValue")
  @Mapping(target = "dateValue", source = "dateValue")
  @Mapping(target = "booleanValue", source = "booleanValue")
  ProductAttributeValueResponse toAttributeValueResponse(ProductAttributeValue value);

  /**
   * Maps a product and its prepared related data to a response.
   *
   * <p>The facade prepares related collections and derived prices so this mapper remains limited to
   * field mapping.
   *
   * @param product product to map
   * @param categoryIds associated category identifiers
   * @param attributes mapped attribute values
   * @param stockByWarehouse mapped stock balances
   * @param secondaryNetPrice calculated secondary-unit price
   * @return mapped product response
   */
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "product.id")
  @Mapping(target = "sku", source = "product.sku")
  @Mapping(target = "ean", source = "product.ean")
  @Mapping(target = "name", source = "product.name")
  @Mapping(target = "description", source = "product.description")
  @Mapping(target = "brand", expression = "java(toBrandResponse(product.getBrand()))")
  @Mapping(target = "status", source = "product.status")
  @Mapping(target = "units", expression = "java(toUnits(product))")
  @Mapping(target = "pricing", expression = "java(toPricing(product, secondaryNetPrice))")
  @Mapping(target = "dimensions", expression = "java(toDimensions(product))")
  @Mapping(target = "categoryIds", source = "categoryIds")
  @Mapping(target = "attributes", source = "attributes")
  @Mapping(target = "stockByWarehouse", source = "stockByWarehouse")
  @Mapping(target = "createdAt", source = "product.createdAt")
  @Mapping(target = "updatedAt", source = "product.updatedAt")
  @Mapping(target = "deletedAt", source = "product.deletedAt")
  ProductResponse toProductResponse(
      Product product,
      List<Long> categoryIds,
      List<ProductAttributeValueResponse> attributes,
      List<ProductStockResponse> stockByWarehouse,
      BigDecimal secondaryNetPrice);

  /**
   * Maps an optional brand relation to its API representation.
   *
   * @param brand optional brand relation
   * @return mapped brand, or {@code null} when no brand is assigned
   */
  default BrandResponse toBrandResponse(final Brand brand) {
    return brand == null ? null : new BrandResponse(brand.getId(), brand.getName());
  }

  /**
   * Maps the product's configured units.
   *
   * @param product product containing unit configuration
   * @return mapped unit configuration
   */
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "main", source = "unit")
  @Mapping(target = "secondary", source = "secondaryUnit")
  @Mapping(target = "secondaryUnitsPerMainUnit", source = "secondaryUnitsPerMainUnit")
  ProductResponse.Units toUnits(Product product);

  /**
   * Maps product pricing and a secondary-unit price calculated by the facade.
   *
   * @param product product containing pricing values
   * @param secondaryNetPrice calculated secondary-unit price
   * @return mapped product pricing
   */
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "currencyId", source = "product.currency.id")
  @Mapping(target = "netPrice", source = "product.netPrice")
  @Mapping(target = "secondaryNetPrice", source = "secondaryNetPrice")
  @Mapping(target = "costPrice", source = "product.costPrice")
  @Mapping(target = "vatRate", source = "product.vatRate")
  ProductResponse.Pricing toPricing(Product product, BigDecimal secondaryNetPrice);

  /**
   * Maps product dimensions.
   *
   * @param product product containing dimensions
   * @return mapped dimensions
   */
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "weight", source = "weight")
  @Mapping(target = "width", source = "width")
  @Mapping(target = "height", source = "height")
  @Mapping(target = "depth", source = "depth")
  ProductResponse.Dimensions toDimensions(Product product);

  /**
   * Maps a calculated stock quantity and its unit.
   *
   * @param amount calculated amount
   * @param unit unit used for the amount
   * @return mapped stock quantity
   */
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "amount", source = "amount")
  @Mapping(target = "unit", source = "unit")
  ProductStockResponse.Quantity toStockQuantity(BigDecimal amount, Unit unit);

  /**
   * Maps prepared quantities to the stock display breakdown.
   *
   * @param fullMainUnits stock expressed as full main units
   * @param secondaryUnitRemainder remaining secondary units
   * @return mapped stock breakdown
   */
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "fullMainUnits", source = "fullMainUnits")
  @Mapping(target = "secondaryUnitRemainder", source = "secondaryUnitRemainder")
  ProductStockResponse.StockBreakdown toStockBreakdown(
      ProductStockResponse.Quantity fullMainUnits,
      ProductStockResponse.Quantity secondaryUnitRemainder);

  /**
   * Maps warehouse details and prepared stock quantities to a response.
   *
   * @param balance stock balance containing warehouse details
   * @param stockQuantity exact stock quantity
   * @param displayQuantity stock quantity for display
   * @return mapped stock response
   */
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "warehouseId", source = "balance.warehouse.id")
  @Mapping(target = "warehouseName", source = "balance.warehouse.name")
  @Mapping(target = "stockQuantity", source = "stockQuantity")
  @Mapping(target = "displayQuantity", source = "displayQuantity")
  ProductStockResponse toStockResponse(
      StockBalance balance,
      ProductStockResponse.Quantity stockQuantity,
      ProductStockResponse.StockBreakdown displayQuantity);
}
