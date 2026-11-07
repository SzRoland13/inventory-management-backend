package dev.roland.inventory_management_backend.features.document.facade.workflow;

import java.math.BigDecimal;
import java.util.function.Supplier;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.features.auth.CustomUserDetails;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.dto.DocumentResponse;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentRelationType;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.exception.DocumentConflictException;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document.service.DocumentService;
import dev.roland.inventory_management_backend.features.document.utils.DocumentWorkflowValues;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.document_line_allocation.service.DocumentLineAllocationService;
import dev.roland.inventory_management_backend.features.document_relation.service.DocumentRelationService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.user.service.UserService;
import lombok.RequiredArgsConstructor;

/** Resolves the current actor and enforces document access and workflow policies. */
@Service
@RequiredArgsConstructor
public class DocumentWorkflowAccessService {
  private final DocumentService documentService;
  private final CurrentCompanyProvider companyProvider;
  private final UserService userService;
  private final DocumentLineAllocationService allocationService;
  private final DocumentRelationService relationService;

  /**
   * Returns the authenticated user, or {@code null} when no application user is authenticated.
   *
   * @return the resulting value
   */
  public User currentActor() {
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    User actor = null;
    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails principal) {
      actor = userService.findByIdOrThrow(principal.getUserId());
    }
    return actor;
  }

  /**
   * Finds a document in the current company.
   *
   * @param id the id value
   * @return the resulting value
   */
  public Document find(final Long id) {
    return documentService
        .findByIdAndCompanyId(id, companyProvider.getCurrentCompany().getId())
        .orElseThrow(this::notFound);
  }

  /**
   * Locks and returns a document belonging to the specified company.
   *
   * @param companyId the companyId value
   * @param id the id value
   * @return the resulting value
   */
  public Document lock(final Long id, final Long companyId) {
    return documentService.lockByIdAndCompanyId(id, companyId).orElseThrow(this::notFound);
  }

  /**
   * Creates the standard not-found exception for documents.
   *
   * @return the resulting value
   */
  public NotFoundException notFound() {
    return new NotFoundException(NotFoundMessageKey.DOCUMENT);
  }

  /**
   * Checks whether a user may create documents of the supplied type.
   *
   * @param type the type value
   * @param user the user value
   * @return the resulting value
   */
  public boolean canCreateType(final DocumentType type, final User user) {
    return user != null
        && (user.getRole() == UserRole.ADMIN
            || user.getRole() == UserRole.MANAGER
            || (user.getRole() == UserRole.SALES
                && (type == DocumentType.SALES_ORDER
                    || type == DocumentType.DELIVERY_NOTE
                    || type == DocumentType.CUSTOMER_INVOICE)));
  }

  /**
   * Checks whether a user may manage the supplied document.
   *
   * @param document the document value
   * @param user the user value
   * @return the resulting value
   */
  public boolean canManageDocument(final Document document, final User user) {
    return user != null
        && (user.getRole() == UserRole.ADMIN
            || user.getRole() == UserRole.MANAGER
            || (document.getCreatedByUser() != null
                && document.getCreatedByUser().getId().equals(user.getId())));
  }

  /**
   * Checks whether a user may perform a direct operation for a document type.
   *
   * @param type the type value
   * @param user the user value
   * @return the resulting value
   */
  public boolean directAllowed(final DocumentType type, final User user) {
    return user != null
        && (user.getRole() == UserRole.ADMIN
            || user.getRole() == UserRole.MANAGER
            || (type == DocumentType.DELIVERY_NOTE && user.getRole() == UserRole.SALES));
  }

  /**
   * Checks whether the document type supports direct operations.
   *
   * @param type the type value
   * @return the resulting value
   */
  public boolean isDirectOperation(final DocumentType type) {
    return type == DocumentType.GOODS_RECEIPT || type == DocumentType.DELIVERY_NOTE;
  }

  /**
   * Checks whether the source document matches the requested fulfillment operation.
   *
   * @param operation the operation value
   * @param source the source value
   * @return the resulting value
   */
  public boolean isMatchingOrder(final Document source, final DocumentType operation) {
    return operation == DocumentType.GOODS_RECEIPT
        ? source.getType() == DocumentType.PURCHASE_ORDER
        : source.getType() == DocumentType.SALES_ORDER;
  }

  /**
   * Checks whether a fulfillment relation exists for the document.
   *
   * @param document the document value
   * @return the resulting value
   */
  public boolean hasFulfillmentSource(final Document document) {
    return relationService
        .findAllBySourceDocumentIdOrTargetDocumentId(document.getId(), document.getId())
        .stream()
        .anyMatch(
            relation ->
                relation.getRelationType() == DocumentRelationType.FULFILLS
                    && relation.getTargetDocument().getId().equals(document.getId()));
  }

  /**
   * Returns the quantity allocated to completed fulfillment documents for a line.
   *
   * @param line the line value
   * @return the resulting value
   */
  public BigDecimal fulfilledFor(final DocumentLine line) {
    return line.getId() == null
        ? DocumentWorkflowValues.nz(line.getFulfilledQuantity())
        : DocumentWorkflowValues.nz(
            allocationService.sumPostedAllocated(line.getId(), DocumentStatus.COMPLETED));
  }

  /**
   * Checks whether any document line has quantity remaining to fulfill.
   *
   * @param document the document value
   * @return the resulting value
   */
  public boolean hasRemaining(final Document document) {
    return DocumentWorkflowValues.safe(document.getLines()).stream()
        .anyMatch(
            line ->
                DocumentWorkflowValues.nz(line.getQuantity())
                        .compareTo(
                            fulfilledFor(line)
                                .add(DocumentWorkflowValues.nz(line.getClosedQuantity())))
                    > 0);
  }

  /**
   * * * * * Validates the supplied version against the document's current version.
   *
   * @param document the document value
   * @param latestResponse the latestResponse value
   * @param suppliedVersion the suppliedVersion value
   * @throws DocumentConflictException when the operation cannot be completed
   * @throws dev.roland.inventory_management_backend.common.exception.ApiException when the
   *     operation cannot be completed
   */
  public void checkVersion(
      final Document document,
      final String suppliedVersion,
      final Supplier<DocumentResponse> latestResponse) {
    if (suppliedVersion == null || suppliedVersion.isBlank()) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_VERSION_REQUIRED);
    }
    String version = suppliedVersion.trim();
    if (version.startsWith("W/")) {
      version = version.substring(2);
    }
    version = version.replace("\"", "");
    if (!java.util.Objects.equals(String.valueOf(document.getVersion()), version)) {
      throw new DocumentConflictException(latestResponse.get());
    }
  }
}
