package me.danielx.api.transactions.dto;

import me.danielx.api.risk.RiskAssessment;
import me.danielx.api.risk.RiskLevel;
import me.danielx.api.transactions.SourceType;
import me.danielx.api.transactions.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public record TransactionResponse(
    UUID id,
    UUID accountId,
    String merchant,
    BigDecimal amount,
    String currency,
    Instant date,
    SourceType sourceType,
    Integer riskScore,
    String riskLevel,
    String riskReason,
    boolean pending) {

  public static TransactionResponse from(Transaction transaction, RiskAssessment assessment) {
    return new TransactionResponse(
        transaction.getPublicId(),
        transaction.getAccount().getPublicId(),
        transaction.effectiveMerchant(),
        transaction.getAmount(),
        transaction.getCurrencyCode(),
        transaction.effectiveDate(),
        transaction.getSourceType(),
        assessment == null ? null : assessment.getScore(),
        assessment == null ? null : toApiLevel(assessment.getRiskLevel()),
        assessment == null ? null : assessment.getPrimaryReason(),
        transaction.isPending());
  }

  public static String toApiLevel(RiskLevel level) {
    return level.name().toLowerCase(Locale.ROOT);
  }
}
