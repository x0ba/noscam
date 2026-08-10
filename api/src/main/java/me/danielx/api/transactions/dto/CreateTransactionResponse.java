package me.danielx.api.transactions.dto;

import lombok.Builder;
import me.danielx.api.risk.RiskAssessment;
import me.danielx.api.transactions.SourceType;
import me.danielx.api.transactions.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record CreateTransactionResponse(
    UUID id,
    UUID accountId,
    String merchant,
    BigDecimal amount,
    String currency,
    Instant date,
    SourceType sourceType,
    Integer riskScore,
    String riskLevel,
    String riskReason) {

  public static CreateTransactionResponse from(Transaction transaction, RiskAssessment assessment) {
    return CreateTransactionResponse.builder()
        .id(transaction.getPublicId())
        .accountId(transaction.getAccount().getPublicId())
        .merchant(transaction.effectiveMerchant())
        .amount(transaction.getAmount())
        .currency(transaction.getCurrencyCode())
        .date(transaction.effectiveDate())
        .sourceType(transaction.getSourceType())
        .riskScore(assessment == null ? null : assessment.getScore())
        .riskLevel(assessment == null ? null : TransactionResponse.toApiLevel(assessment.getRiskLevel()))
        .riskReason(assessment == null ? null : assessment.getPrimaryReason())
        .build();
  }
}
