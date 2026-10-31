package dev.roland.inventory_management_backend.features.document.dto;

import java.time.LocalDateTime;

/**
 * Defines the activity document API contract.
 *
 * @param actorName actor name value
 * @param description description value
 * @param id id value
 * @param occurredAt occurred time value
 */
public record Activity(Long id, String description, String actorName, LocalDateTime occurredAt) {}
