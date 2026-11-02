package dev.roland.inventory_management_backend.features.document.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.common.service.ObjectStorageService;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentAction;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentRelationType;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.enumeration.ReturnDirection;
import dev.roland.inventory_management_backend.features.document.enumeration.ReturnDisposition;
import dev.roland.inventory_management_backend.features.document.facade.workflow.DocumentWorkflowAccessService;
import dev.roland.inventory_management_backend.features.document_activity.DocumentActivity;
import dev.roland.inventory_management_backend.features.document_activity.DocumentActivityEvent;
import dev.roland.inventory_management_backend.features.document_activity.service.DocumentActivityService;
import dev.roland.inventory_management_backend.features.document_file.DocumentFile;
import dev.roland.inventory_management_backend.features.document_file.DocumentFileSource;
import dev.roland.inventory_management_backend.features.document_file.DocumentFileStatus;
import dev.roland.inventory_management_backend.features.document_file.service.DocumentFileService;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.document_line_allocation.service.DocumentLineAllocationService;
import dev.roland.inventory_management_backend.features.document_relation.DocumentRelation;
import dev.roland.inventory_management_backend.features.document_relation.service.DocumentRelationService;
import dev.roland.inventory_management_backend.features.media_usage.service.MediaUsageService;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;

@ExtendWith(MockitoExtension.class)
class DocumentResponseMapperTest {
  @Mock private DocumentWorkflowAccessService accessService;
  @Mock private DocumentLineAllocationService allocationService;
  @Mock private DocumentRelationService relationService;
  @Mock private DocumentActivityService activityService;
  @Mock private MediaUsageService mediaUsageService;
  @Mock private DocumentFileService fileService;
  @Mock private ObjectStorageService objectStorage;

  private DocumentResponseMapper mapper;
  private User manager;

  @BeforeEach
  void setUp() {
    mapper =
        new DocumentResponseMapper(
            accessService,
            allocationService,
            relationService,
            activityService,
            mediaUsageService,
            fileService,
            objectStorage);
    manager = User.builder().id(5L).username("manager").role(UserRole.MANAGER).build();
    lenient().when(accessService.currentActor()).thenReturn(manager);
    lenient().when(accessService.canManageDocument(any(), any())).thenReturn(true);
    lenient().when(accessService.directAllowed(any(), any())).thenReturn(true);
    lenient().when(accessService.hasRemaining(any())).thenReturn(true);
    lenient().when(accessService.hasFulfillmentSource(any())).thenReturn(false);
    lenient().when(accessService.fulfilledFor(any())).thenReturn(BigDecimal.ZERO);
    lenient()
        .when(relationService.findAllBySourceDocumentIdOrTargetDocumentId(any(), any()))
        .thenReturn(List.of());
    lenient()
        .when(activityService.findAllByDocumentIdOrderByOccurredAtAsc(any()))
        .thenReturn(List.of());
    lenient()
        .when(
            mediaUsageService.findAllByEntityTypeAndEntityIdAndUsageTypeOrderBySortOrderAsc(
                any(), any(), any()))
        .thenReturn(List.of());
    lenient()
        .when(fileService.findAllByDocumentIdOrderByCreatedAtDesc(any()))
        .thenReturn(List.of());
    lenient().when(allocationService.findAllByFulfillmentLineId(any())).thenReturn(List.of());
  }

  @Test
  void mapsDocumentAndItsDirectOperationLine() {
    final Document document = document(DocumentType.DELIVERY_NOTE, DocumentStatus.COMPLETED);
    final DocumentLine line = new DocumentLine();
    line.setId(12L);
    line.setProduct(Product.builder().id(13L).build());
    line.setQuantity(new BigDecimal("3"));
    line.setUnitPrice(new BigDecimal("4"));
    line.setReturnedQuantity(BigDecimal.ONE);
    document.setLines(List.of(line));

    final var response = mapper.toResponse(document);

    assertEquals(1, response.lines().size());
    assertEquals(new BigDecimal("3"), response.lines().getFirst().operationQuantity());
    assertTrue(response.allowedActions().contains(DocumentAction.CREATE_RETURN));
    assertTrue(response.allowedActions().contains(DocumentAction.CORRECT));
  }

  @Test
  void exposesDraftAndConfirmedOrderActionsAndDirectPosting() {
    final Document draftOrder = document(DocumentType.PURCHASE_ORDER, DocumentStatus.DRAFT);
    assertTrue(mapper.allowedActions(draftOrder).contains(DocumentAction.EDIT));
    assertTrue(mapper.allowedActions(draftOrder).contains(DocumentAction.CONFIRM));
    assertTrue(mapper.allowedActions(draftOrder).contains(DocumentAction.UPLOAD_FILE));

    final Document confirmedOrder = document(DocumentType.PURCHASE_ORDER, DocumentStatus.CONFIRMED);
    assertTrue(mapper.allowedActions(confirmedOrder).contains(DocumentAction.POST));
    assertTrue(mapper.allowedActions(confirmedOrder).contains(DocumentAction.CLOSE_REMAINING));

    final Document directDraft = document(DocumentType.GOODS_RECEIPT, DocumentStatus.DRAFT);
    assertTrue(mapper.allowedActions(directDraft).contains(DocumentAction.POST));
  }

  @Test
  void exposesTransferCountReturnAndQuarantineActions() {
    final Document transfer = document(DocumentType.TRANSFER_REQUEST, DocumentStatus.DRAFT);
    final DocumentLine transferLine = new DocumentLine();
    transferLine.setQuantity(new BigDecimal("5"));
    transferLine.setFulfilledQuantity(BigDecimal.ONE);
    transferLine.setInTransitQuantity(BigDecimal.ONE);
    transfer.setLines(List.of(transferLine));
    assertTrue(mapper.allowedActions(transfer).contains(DocumentAction.DISPATCH));
    assertTrue(mapper.allowedActions(transfer).contains(DocumentAction.RECEIVE_TRANSFER));
    assertTrue(mapper.allowedActions(transfer).contains(DocumentAction.RESOLVE_TRANSIT));

    final Document count = document(DocumentType.STOCK_COUNT, DocumentStatus.DRAFT);
    assertTrue(mapper.allowedActions(count).contains(DocumentAction.RECONCILE));

    final Document returned = document(DocumentType.RETURN, DocumentStatus.COMPLETED);
    returned.setWorkflowDirection(ReturnDirection.CUSTOMER_RETURN);
    final DocumentLine returnLine = new DocumentLine();
    returnLine.setQuantity(new BigDecimal("2"));
    returnLine.setWorkflowDisposition(ReturnDisposition.DAMAGED);
    returnLine.setReleasedQuantity(BigDecimal.ZERO);
    returned.setLines(List.of(returnLine));
    final List<DocumentAction> actions = mapper.allowedActions(returned);
    assertTrue(actions.contains(DocumentAction.RELEASE_QUARANTINE));
    assertTrue(actions.contains(DocumentAction.CREATE_REPLACEMENT_DELIVERY));
    assertTrue(actions.contains(DocumentAction.GENERATE_PDF));
  }

  @Test
  void salesRoleCannotUseWarehouseActionsAndCancelledDocumentsAreRestricted() {
    when(accessService.currentActor()).thenReturn(User.builder().role(UserRole.SALES).build());
    final Document returned = document(DocumentType.RETURN, DocumentStatus.COMPLETED);
    returned.setWorkflowDirection(ReturnDirection.CUSTOMER_RETURN);
    final List<DocumentAction> salesActions = mapper.allowedActions(returned);
    assertFalse(salesActions.contains(DocumentAction.CREATE_REPLACEMENT_DELIVERY));
    assertFalse(salesActions.contains(DocumentAction.RELEASE_QUARANTINE));

    when(accessService.currentActor()).thenReturn(manager);
    final Document cancelled = document(DocumentType.DELIVERY_NOTE, DocumentStatus.CANCELLED);
    final List<DocumentAction> cancelledActions = mapper.allowedActions(cancelled);
    assertFalse(cancelledActions.contains(DocumentAction.UPLOAD_FILE));
    assertFalse(cancelledActions.contains(DocumentAction.GENERATE_PDF));
  }

  @Test
  void exposesRetryForLatestFailedPdfAndHandlesCompletedTransferStates() {
    final Document completed = document(DocumentType.DELIVERY_NOTE, DocumentStatus.COMPLETED);
    final DocumentFile failedPdf = new DocumentFile();
    failedPdf.setSource(DocumentFileSource.GENERATED_PDF);
    failedPdf.setStatus(DocumentFileStatus.FAILED);
    when(fileService.findAllByDocumentIdOrderByCreatedAtDesc(completed.getId()))
        .thenReturn(List.of(failedPdf));
    final List<DocumentAction> failedPdfActions = mapper.allowedActions(completed);
    assertTrue(failedPdfActions.contains(DocumentAction.RETRY_PDF));

    final Document cancelledTransfer =
        document(DocumentType.TRANSFER_REQUEST, DocumentStatus.CANCELLED);
    assertFalse(mapper.allowedActions(cancelledTransfer).contains(DocumentAction.DISPATCH));

    final Document closedReturn = document(DocumentType.RETURN, DocumentStatus.COMPLETED);
    closedReturn.setWorkflowDirection(ReturnDirection.SUPPLIER_RETURN);
    final DocumentLine closedLine = new DocumentLine();
    closedLine.setQuantity(BigDecimal.ONE);
    closedLine.setWorkflowDisposition(ReturnDisposition.RESTOCK);
    closedLine.setReleasedQuantity(BigDecimal.ONE);
    closedReturn.setLines(List.of(closedLine));
    assertFalse(mapper.allowedActions(closedReturn).contains(DocumentAction.RELEASE_QUARANTINE));
  }

  @Test
  void omitsRoleSpecificActionsWhenNoActorIsPresent() {
    when(accessService.currentActor()).thenReturn(null);
    final Document returned = document(DocumentType.RETURN, DocumentStatus.COMPLETED);
    returned.setWorkflowDirection(ReturnDirection.CUSTOMER_RETURN);

    final List<DocumentAction> actions = mapper.allowedActions(returned);

    assertFalse(actions.contains(DocumentAction.CREATE_REPLACEMENT_DELIVERY));
    assertFalse(actions.contains(DocumentAction.RELEASE_QUARANTINE));
  }

  @Test
  void suppressesEditWhenDocumentHasFulfillmentAndWhenActorCannotManage() {
    final Document draft = document(DocumentType.SALES_ORDER, DocumentStatus.DRAFT);
    when(accessService.hasFulfillmentSource(draft)).thenReturn(true);
    assertFalse(mapper.allowedActions(draft).contains(DocumentAction.EDIT));

    when(accessService.canManageDocument(draft, manager)).thenReturn(false);
    final List<DocumentAction> actions = mapper.allowedActions(draft);
    assertFalse(actions.contains(DocumentAction.EDIT));
    assertFalse(actions.contains(DocumentAction.CONFIRM));
    assertFalse(actions.contains(DocumentAction.UPLOAD_FILE));
  }

  @Test
  void mapsRelatedDocumentsActivitiesAndGeneratedPdfFallbacks() {
    final Document document = document(DocumentType.TRANSFER_DELIVERY, DocumentStatus.COMPLETED);
    final Document linked = document(DocumentType.TRANSFER_REQUEST, DocumentStatus.COMPLETED);
    linked.setId(2L);
    linked.setDocumentNumber("TR-1");
    final DocumentRelation outbound =
        DocumentRelation.builder()
            .sourceDocument(document)
            .targetDocument(linked)
            .relationType(DocumentRelationType.TRANSFER_STEP)
            .build();
    final DocumentRelation inbound =
        DocumentRelation.builder()
            .sourceDocument(linked)
            .targetDocument(document)
            .relationType(DocumentRelationType.TRANSFER_STEP)
            .build();
    when(relationService.findAllBySourceDocumentIdOrTargetDocumentId(1L, 1L))
        .thenReturn(List.of(outbound, inbound));
    final DocumentActivity systemActivity = new DocumentActivity();
    systemActivity.setId(1L);
    systemActivity.setEventType(DocumentActivityEvent.POSTED);
    final DocumentActivity userActivity = new DocumentActivity();
    userActivity.setId(2L);
    userActivity.setEventType(DocumentActivityEvent.EDITED);
    userActivity.setDetails("updated");
    userActivity.setActor(manager);
    when(activityService.findAllByDocumentIdOrderByOccurredAtAsc(1L))
        .thenReturn(List.of(systemActivity, userActivity));
    final DocumentFile uploaded = new DocumentFile();
    uploaded.setSource(DocumentFileSource.UPLOAD);
    final DocumentFile generated = new DocumentFile();
    generated.setSource(DocumentFileSource.GENERATED_PDF);
    generated.setLanguageTag("en");
    when(fileService.findAllByDocumentIdOrderByCreatedAtDesc(1L))
        .thenReturn(List.of(uploaded, generated));

    final var response = mapper.toResponse(document);

    assertEquals(1, response.relatedDocuments().size());
    assertEquals(2, response.activity().size());
    assertEquals("System", response.activity().getFirst().actorName());
    assertEquals("document.pdf", response.files().getFirst().fileName());
    assertEquals("application/pdf", response.files().getFirst().contentType());
  }

  private Document document(final DocumentType type, final DocumentStatus status) {
    final Document document = new Document();
    document.setId(1L);
    document.setType(type);
    document.setStatus(status);
    document.setVersion(0L);
    document.setDocumentDate(LocalDate.of(2026, 10, 1));
    document.setLines(List.of());
    return document;
  }
}
