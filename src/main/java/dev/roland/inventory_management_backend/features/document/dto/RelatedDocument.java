package dev.roland.inventory_management_backend.features.document.dto;

import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;

/**
 * Defines the related document API contract.
 *
 * @param id id value
 * @param number number value
 * @param relationKind relation kind value
 * @param type type value
 */
public record RelatedDocument(Long id, String number, DocumentType type, String relationKind) {}
