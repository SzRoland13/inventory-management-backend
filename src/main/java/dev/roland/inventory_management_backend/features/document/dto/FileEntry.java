package dev.roland.inventory_management_backend.features.document.dto;

import java.time.LocalDateTime;

/**
 * Defines the file entry document API contract.
 *
 * @param contentType content type value
 * @param createdAt created time value
 * @param downloadUrl download url value
 * @param errorMessage error message value
 * @param fileName file name value
 * @param id id value
 * @param language language value
 * @param source source value
 * @param status status value
 */
public record FileEntry(
    Long id,
    String fileName,
    String contentType,
    String source,
    String status,
    String language,
    String errorMessage,
    String downloadUrl,
    LocalDateTime createdAt) {}
