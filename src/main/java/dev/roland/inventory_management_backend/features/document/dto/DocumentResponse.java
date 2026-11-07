package dev.roland.inventory_management_backend.features.document.dto;

import static dev.roland.inventory_management_backend.features.document.dto.DocumentApiDtoCollections.immutableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import dev.roland.inventory_management_backend.features.document.enumeration.CommercialApprovalStatus;
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
 * @param calculationVersion the calculationVersion value
 * @param countedAt the countedAt value
 * @param destinationWarehouseId the destinationWarehouseId value
 * @param direction the direction value
 * @param disposition the disposition value
 * @param nonBillable the nonBillable value
 * @param reason the reason value
 * @param sourceWarehouseId the sourceWarehouseId value
 * @param transitWarehouseId the transitWarehouseId value
 * @param paymentTerms payment terms snapshot
 * @param approvalRequired whether price or VAT approval is required
 * @param approvalStatus commercial approval state
 * @param approvalComment approval or rejection comment
 * @param approvedBy approving user name
 * @param approvedAt approval timestamp
 * @param financialSettlementRequired whether settlement follow-up is required
 * @param financialSettlementOwnerId settlement owner identifier
 * @param financialSettlementOwnerName settlement owner name
 * @param referenceNumber external reference number
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
    Long sourceWarehouseId,
    Long destinationWarehouseId,
    Long transitWarehouseId,
    LocalDateTime countedAt,
    String reason,
    String disposition,
    String direction,
    String calculationVersion,
    Boolean nonBillable,
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
    List<FileEntry> files,
    String paymentTerms,
    boolean approvalRequired,
    CommercialApprovalStatus approvalStatus,
    String approvalComment,
    String approvedBy,
    LocalDateTime approvedAt,
    boolean financialSettlementRequired,
    Long financialSettlementOwnerId,
    String financialSettlementOwnerName,
    String referenceNumber) {
  /** Keeps existing document workflow callers source-compatible. */
  public DocumentResponse(
      final Long id,
      final DocumentType type,
      final String number,
      final LocalDate date,
      final String ownerName,
      final String partnerName,
      final String routeName,
      final Long partnerId,
      final Long warehouseId,
      final Long sourceWarehouseId,
      final Long destinationWarehouseId,
      final Long transitWarehouseId,
      final LocalDateTime countedAt,
      final String reason,
      final String disposition,
      final String direction,
      final String calculationVersion,
      final Boolean nonBillable,
      final String notes,
      final String currencyCode,
      final Long currencyId,
      final List<ExchangeRateResponse> exchangeRates,
      final DocumentTotals totals,
      final String status,
      final String version,
      final List<LineResponse> lines,
      final List<DocumentAction> allowedActions,
      final List<RelatedDocument> relatedDocuments,
      final List<Activity> activity,
      final List<FileEntry> files) {
    this(
        id,
        type,
        number,
        date,
        ownerName,
        partnerName,
        routeName,
        partnerId,
        warehouseId,
        sourceWarehouseId,
        destinationWarehouseId,
        transitWarehouseId,
        countedAt,
        reason,
        disposition,
        direction,
        calculationVersion,
        nonBillable,
        notes,
        currencyCode,
        currencyId,
        exchangeRates,
        totals,
        status,
        version,
        lines,
        allowedActions,
        relatedDocuments,
        activity,
        files,
        null,
        false,
        null,
        null,
        null,
        null,
        false,
        null,
        null,
        null);
  }

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
