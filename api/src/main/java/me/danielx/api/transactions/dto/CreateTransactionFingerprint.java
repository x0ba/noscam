package me.danielx.api.transactions.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.UUID;

@JsonPropertyOrder({
  "accountId",
  "amount",
  "currency",
  "merchant",
  "displayName",
  "originalDescription",
  "authorizedAt",
  "postedAt",
  "pending",
  "category",
  "paymentChannel",
  "merchantCountry"
})
public record CreateTransactionFingerprint(
    UUID accountId,
    String amount,
    String currency,
    String merchant,
    String displayName,
    String originalDescription,
    String authorizedAt,
    String postedAt,
    Boolean pending,
    String category,
    String paymentChannel,
    String merchantCountry) {}
