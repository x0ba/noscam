package me.danielx.api.transactions.dto;

import me.danielx.api.risk.RiskAssessment;
import me.danielx.api.risk.RiskFactorResult;
import me.danielx.api.transactions.SourceType;
import me.danielx.api.transactions.Transaction;
import me.danielx.api.transactions.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record TransactionDetailResponse(
    UUID id,
    UUID accountId,
    String merchant,
    String displayName,
    String originalDescription,
    BigDecimal amount,
    String currency,
    Instant date,
    Instant authorizedAt,
    Instant postedAt,
    SourceType sourceType,
    TransactionStatus status,
    boolean pending,
    String category,
    String paymentChannel,
    String merchantCountry,
    Integer riskScore,
    String riskLevel,
    String riskReason,
    Integer engineVersion,
    Integer configVersion,
    Instant scoredAt,
    List<FactorResultResponse> factors) {

  public static TransactionDetailResponse from(Transaction transaction, RiskAssessment assessment) {
    List<FactorResultResponse> factors =
        assessment == null
            ? List.of()
            : assessment.getFactorResults().stream().map(FactorResultResponse::from).toList();
    return new TransactionDetailResponse(
        transaction.getPublicId(),
        transaction.getAccount().getPublicId(),
        transaction.effectiveMerchant(),
        transaction.getDisplayName(),
        transaction.getOriginalDescription(),
        transaction.getAmount(),
        transaction.getCurrencyCode(),
        transaction.effectiveDate(),
        transaction.getAuthorizedAt(),
        transaction.getPostedAt(),
        transaction.getSourceType(),
        transaction.getStatus(),
        transaction.isPending(),
        transaction.getCategory(),
        transaction.getPaymentChannel(),
        transaction.getMerchantCountry(),
        assessment == null ? null : assessment.getScore(),
        assessment == null ? null : TransactionResponse.toApiLevel(assessment.getRiskLevel()),
        assessment == null ? null : assessment.getPrimaryReason(),
        assessment == null ? null : assessment.getEngineVersion(),
        assessment == null ? null : assessment.getConfigVersion(),
        assessment == null ? null : assessment.getScoredAt(),
        factors);
  }

  public record FactorResultResponse(
      String key, int points, boolean matched, String explanation, Map<String, Object> evidence) {
    public static FactorResultResponse from(RiskFactorResult result) {
      return new FactorResultResponse(
          result.getFactorKey(),
          result.getPoints(),
          result.isMatched(),
          result.getExplanation(),
          result.getEvidence());
    }
  }
}
