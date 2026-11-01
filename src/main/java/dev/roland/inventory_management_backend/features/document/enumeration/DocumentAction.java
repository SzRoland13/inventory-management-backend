package dev.roland.inventory_management_backend.features.document.enumeration;

/** Actions the current user is allowed to perform on a document. */
public enum DocumentAction {
  EDIT,
  CONFIRM,
  POST,
  UPLOAD_FILE,
  GENERATE_PDF,
  RETRY_PDF,
  CLOSE_REMAINING
}
