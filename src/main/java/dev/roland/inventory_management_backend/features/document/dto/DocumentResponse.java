package dev.roland.inventory_management_backend.features.document.dto;

import static dev.roland.inventory_management_backend.features.document.dto.DocumentApiDtoCollections.immutableList;

import java.time.LocalDate;
import java.util.List;

import dev.roland.inventory_management_backend.features.document.enumeration.DocumentAction;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;

/**
 * Defines the document response document API contract.
 *
 * @param activity activity value
 * @param allowedActions allowed actions value
 * @param currencyCode currency code value
 * @param currencyId currency identifier value
 * @param exchangeRates saved exchange-rate snapshots
 * @param date date value
 * @param files files value
 * @param id id value
 * @param lines lines value
 * @param notes notes value
 * @param number number value
 * @param ownerName owner name value
 * @param partnerId partner identifier value
 * @param partnerName partner name value
 * @param relatedDocuments related documents value
 * @param routeName route name value
 * @param status status value
 * @param totals totals value
 * @param type type value
 * @param version version value
 * @param warehouseId warehouse identifier value
 */
public record DocumentResponse(
    Long id,
    DocumentType type,
    String number,
    LocalDate date,
    String ownerName,
    String partnerName,
    String routeName,
    Long partnerId,
    Long warehouseId,
    String notes,
    String currencyCode,
    Long currencyId,
    List<ExchangeRateResponse> exchangeRates,
    DocumentTotals totals,
    String status,
    String version,
    List<LineResponse> lines,
    List<DocumentAction> allowedActions,
    List<RelatedDocument> relatedDocuments,
    List<Activity> activity,
    List<FileEntry> files) {
  /**
   * Copies response collections to prevent external mutation.
   *
   * @param id document identifier
   * @param type document type
   * @param number document number
   * @param date document date
   * @param ownerName owner name
   * @param partnerName partner name
   * @param routeName route name
   * @param partnerId partner identifier
   * @param warehouseId warehouse identifier
   * @param notes document notes
   * @param currencyCode currency code
   * @param totals document totals
   * @param status document status
   * @param version document version
   * @param lines document lines
   * @param allowedActions available actions
   * @param relatedDocuments related documents
   * @param activity document activity
   * @param files document files
   */
  public DocumentResponse {
    lines = immutableList(lines);
    exchangeRates = immutableList(exchangeRates);
    allowedActions = immutableList(allowedActions);
    relatedDocuments = immutableList(relatedDocuments);
    activity = immutableList(activity);
    files = immutableList(files);
  }

  /**
   * Returns a defensive copy of the response lines.
   *
   * @return immutable response lines
   */
  @Override
  public List<LineResponse> lines() {
    return immutableList(lines);
  }

  @Override
  public List<ExchangeRateResponse> exchangeRates() {
    return immutableList(exchangeRates);
  }

  /**
   * Returns a defensive copy of the allowed actions.
   *
   * @return immutable allowed actions
   */
  @Override
  public List<DocumentAction> allowedActions() {
    return immutableList(allowedActions);
  }

  /**
   * Returns a defensive copy of related documents.
   *
   * @return immutable related documents
   */
  @Override
  public List<RelatedDocument> relatedDocuments() {
    return immutableList(relatedDocuments);
  }

  /**
   * Returns a defensive copy of the activity entries.
   *
   * @return immutable activity entries
   */
  @Override
  public List<Activity> activity() {
    return immutableList(activity);
  }

  /**
   * Returns a defensive copy of the file entries.
   *
   * @return immutable file entries
   */
  @Override
  public List<FileEntry> files() {
    return immutableList(files);
  }
}
