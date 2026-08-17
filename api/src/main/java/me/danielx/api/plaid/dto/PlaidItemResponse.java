package me.danielx.api.plaid.dto;

import me.danielx.api.plaid.PlaidItem;
import me.danielx.api.plaid.PlaidItemStatus;

import java.time.Instant;
import java.util.UUID;

public record PlaidItemResponse(
    UUID id,
    String institutionName,
    String institutionId,
    PlaidItemStatus status,
    Instant connectedAt,
    Instant lastSuccessfulSync,
    String lastErrorCode) {

  public static PlaidItemResponse from(PlaidItem item) {
    return new PlaidItemResponse(
        item.getPublicId(),
        item.getInstitutionName(),
        item.getInstitutionId(),
        item.getStatus(),
        item.getConnectedAt(),
        item.getLastSuccessfulSync(),
        item.getLastErrorCode());
  }
}
