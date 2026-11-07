package dev.roland.inventory_management_backend.features.document.mapper;

import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentLine;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentPreview;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.utils.DocumentWorkflowValues;
import lombok.experimental.UtilityClass;

/** Maps a persisted fulfillment draft to the stock preview input. */
@UtilityClass
public final class DocumentFulfillmentMapper {
  /**
   * Maps a fulfillment draft to the stock preview input.
   *
   * @param document the document value
   * @return the resulting value
   */
  public static FulfillmentPreview toPreview(final Document document) {
    return new FulfillmentPreview(
        null,
        document.getId(),
        document.getType(),
        DocumentWorkflowValues.idOf(
            document.getType() == DocumentType.GOODS_RECEIPT
                ? document.getTargetWarehouse()
                : document.getSourceWarehouse()),
        document.getLines().stream()
            .map(
                line ->
                    new FulfillmentLine(
                        line.getId(), line.getProduct().getId(), line.getQuantity()))
            .toList());
  }
}
