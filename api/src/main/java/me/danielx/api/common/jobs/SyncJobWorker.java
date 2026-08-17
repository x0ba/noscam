package me.danielx.api.common.jobs;

import io.micrometer.core.instrument.MeterRegistry;
import me.danielx.api.plaid.PlaidService;
import me.danielx.api.risk.RiskScoringService;
import me.danielx.api.transactions.Transaction;
import me.danielx.api.transactions.TransactionRepository;
import me.danielx.api.transactions.TransactionStatus;
import me.danielx.api.notifications.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class SyncJobWorker {
  private static final Logger log = LoggerFactory.getLogger(SyncJobWorker.class);

  private final SyncJobService syncJobService;
  private final PlaidService plaidService;
  private final TransactionRepository transactionRepository;
  private final RiskScoringService riskScoringService;
  private final MeterRegistry meterRegistry;

  public SyncJobWorker(
      SyncJobService syncJobService,
      PlaidService plaidService,
      TransactionRepository transactionRepository,
      RiskScoringService riskScoringService,
      MeterRegistry meterRegistry) {
    this.syncJobService = syncJobService;
    this.plaidService = plaidService;
    this.transactionRepository = transactionRepository;
    this.riskScoringService = riskScoringService;
    this.meterRegistry = meterRegistry;
  }

  @Scheduled(fixedDelayString = "${app.jobs.poll-interval-ms:2000}")
  public void poll() {
    SyncJob job = syncJobService.claimNext();
    if (job == null) {
      return;
    }
    MDC.put("jobId", job.getPublicId().toString());
    MDC.put("jobType", job.getJobType().name());
    try {
      switch (job.getJobType()) {
        case PLAID_INITIAL_SYNC, PLAID_TRANSACTIONS_SYNC -> plaidService.syncItem(job.getPlaidItem());
        case SETTINGS_RESCORE -> rescoreRecent(job);
      }
      syncJobService.markSucceeded(job);
      meterRegistry.counter("noscam.jobs.succeeded", "type", job.getJobType().name()).increment();
    } catch (Exception ex) {
      log.warn("Sync job failed", ex);
      syncJobService.markFailed(job, ex.getMessage());
      meterRegistry.counter("noscam.jobs.failed", "type", job.getJobType().name()).increment();
    } finally {
      MDC.clear();
    }
  }

  private void rescoreRecent(SyncJob job) {
    Instant since = Instant.now().minus(RiskScoringService.ALERT_LOOKBACK);
    List<Transaction> recent =
        transactionRepository.findByUserIdAndStatusAndPostedAtGreaterThanEqual(
            job.getUser().getId(), TransactionStatus.ACTIVE, since);
    for (Transaction transaction : recent) {
      transaction.setContentHash(null);
      riskScoringService.score(transaction, NotificationType.SETTINGS_RESCORE);
    }
  }
}
