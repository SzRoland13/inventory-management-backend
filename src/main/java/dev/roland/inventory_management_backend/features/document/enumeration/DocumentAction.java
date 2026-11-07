package dev.roland.inventory_management_backend.features.document.enumeration;

/** Actions the current user is allowed to perform on a document. */
public enum DocumentAction {
  EDIT,
  CONFIRM,
  POST,
  UPLOAD_FILE,
  GENERATE_PDF,
  RETRY_PDF,
  CLOSE_REMAINING,
  DISPATCH,
  RECEIVE_TRANSFER,
  RESOLVE_TRANSIT,
  CREATE_RETURN,
  RELEASE_QUARANTINE,
  CREATE_REPLACEMENT_DELIVERY,
  RECONCILE,
  CORRECT,
  APPROVE,
  REJECT,
  FINALIZE,
  ASSIGN_SETTLEMENT
}
