package me.danielx.api.plaid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class PlaidModels {
  private PlaidModels() {}

  public record LinkTokenResult(String linkToken, String expiration) {}

  public record ExchangeResult(
      String accessToken, String itemId, String institutionId, String institutionName) {}

  public record PlaidAccount(
      String accountId,
      String name,
      String officialName,
      String mask,
      String type,
      String subtype,
      BigDecimal currentBalance,
      String isoCurrencyCode) {}

  public record PlaidTransaction(
      String transactionId,
      String accountId,
      String pendingTransactionId,
      BigDecimal amount,
      String isoCurrencyCode,
      String merchantName,
      String name,
      String originalDescription,
      LocalDate date,
      LocalDate authorizedDate,
      boolean pending,
      String paymentChannel,
      List<String> category,
      String merchantCountry) {}

  public record SyncPage(
      List<PlaidTransaction> added,
      List<PlaidTransaction> modified,
      List<PlaidTransaction> removed,
      String nextCursor,
      boolean hasMore) {}
}
