package me.danielx.api.transactions;

import me.danielx.api.notifications.NotificationType;
import me.danielx.api.risk.RiskScoringService;
import me.danielx.api.risk.TransactionContentHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;

@Service
public class TransactionIngestionService {
  private final TransactionRepository transactionRepository;
  private final RiskScoringService riskScoringService;

  public TransactionIngestionService(
      TransactionRepository transactionRepository, RiskScoringService riskScoringService) {
    this.transactionRepository = transactionRepository;
    this.riskScoringService = riskScoringService;
  }

  @Transactional
  public Transaction ingest(IngestedTransaction incoming, NotificationType alertType) {
    Transaction transaction = findOrCreate(incoming);
    apply(transaction, incoming);
    String nextHash = TransactionContentHasher.hash(transaction);
    boolean materialChange =
        transaction.getId() == null || !Objects.equals(transaction.getContentHash(), nextHash);
    transaction.setContentHash(nextHash);
    Transaction saved = transactionRepository.saveAndFlush(transaction);
    if (saved.getStatus() == TransactionStatus.ACTIVE && materialChange) {
      riskScoringService.score(saved, alertType);
    }
    return saved;
  }

  private Transaction findOrCreate(IngestedTransaction incoming) {
    if (incoming.externalId() != null && !incoming.externalId().isBlank()) {
      return transactionRepository
          .findByAccountAndSourceTypeAndExternalId(
              incoming.account(), incoming.sourceType(), incoming.externalId())
          .orElseGet(() -> newTransaction(incoming));
    }
    return newTransaction(incoming);
  }

  private Transaction newTransaction(IngestedTransaction incoming) {
    return Transaction.builder()
        .account(incoming.account())
        .user(incoming.user())
        .sourceType(incoming.sourceType())
        .externalId(incoming.externalId())
        .build();
  }

  private void apply(Transaction transaction, IngestedTransaction incoming) {
    transaction.setAmount(incoming.amount());
    transaction.setCurrencyCode(incoming.currencyCode().toUpperCase(Locale.ROOT));
    transaction.setMerchant(incoming.merchant());
    transaction.setDisplayName(incoming.displayName());
    transaction.setOriginalDescription(incoming.originalDescription());
    transaction.setAuthorizedAt(incoming.authorizedAt());
    transaction.setPostedAt(incoming.postedAt());
    transaction.setPending(incoming.pending());
    transaction.setPendingTransactionId(incoming.pendingTransactionId());
    transaction.setCategory(incoming.category());
    transaction.setPaymentChannel(incoming.paymentChannel());
    transaction.setMerchantCountry(incoming.merchantCountry());
    transaction.setStatus(incoming.status() == null ? TransactionStatus.ACTIVE : incoming.status());
    if (transaction.getStatus() == TransactionStatus.REMOVED && transaction.getRemovedAt() == null) {
      transaction.setRemovedAt(java.time.Instant.now());
    }
  }
}
