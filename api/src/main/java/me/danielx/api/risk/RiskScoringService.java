package me.danielx.api.risk;

import io.micrometer.core.instrument.MeterRegistry;
import me.danielx.api.notifications.NotificationService;
import me.danielx.api.notifications.NotificationType;
import me.danielx.api.transactions.Transaction;
import me.danielx.api.transactions.TransactionRepository;
import me.danielx.api.transactions.TransactionStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RiskScoringService {
  public static final Duration HISTORY_LOOKBACK = Duration.ofDays(180);
  public static final Duration ALERT_LOOKBACK = Duration.ofDays(30);

  private final RiskEngine riskEngine;
  private final RiskSettingsService riskSettingsService;
  private final RiskAssessmentRepository riskAssessmentRepository;
  private final TransactionRepository transactionRepository;
  private final NotificationService notificationService;
  private final MeterRegistry meterRegistry;

  public RiskScoringService(
      RiskEngine riskEngine,
      RiskSettingsService riskSettingsService,
      RiskAssessmentRepository riskAssessmentRepository,
      TransactionRepository transactionRepository,
      NotificationService notificationService,
      MeterRegistry meterRegistry) {
    this.riskEngine = riskEngine;
    this.riskSettingsService = riskSettingsService;
    this.riskAssessmentRepository = riskAssessmentRepository;
    this.transactionRepository = transactionRepository;
    this.notificationService = notificationService;
    this.meterRegistry = meterRegistry;
  }

  @Transactional
  public RiskAssessment score(Transaction transaction, NotificationType alertType) {
    String contentHash = TransactionContentHasher.hash(transaction);
    transaction.setContentHash(contentHash);
    return riskAssessmentRepository
        .findByTransactionAndContentHash(transaction, contentHash)
        .orElseGet(() -> persistScore(transaction, contentHash, alertType));
  }

  private RiskAssessment persistScore(
      Transaction transaction, String contentHash, NotificationType alertType) {
    RiskSettings settings = riskSettingsService.getOrCreate(transaction.getUser());
    Instant since = Instant.now().minus(HISTORY_LOOKBACK);
    List<Transaction> accountHistory =
        transactionRepository.findActiveHistory(transaction.getAccount().getId(), since);
    List<Transaction> userHistory =
        transactionRepository.findActiveUserHistory(transaction.getUser().getId(), since);
    RiskEngine.ScoredResult result =
        riskEngine.score(transaction, accountHistory, userHistory, settings);

    RiskAssessment assessment =
        RiskAssessment.builder()
            .transaction(transaction)
            .user(transaction.getUser())
            .score(result.score())
            .riskLevel(result.level())
            .primaryReason(result.primaryReason())
            .engineVersion(settings.getEngineVersion())
            .configVersion(settings.getConfigVersion())
            .configSnapshot(snapshot(settings))
            .contentHash(contentHash)
            .scoredAt(Instant.now())
            .build();
    assessment.setFactorResults(
        result.contributions().stream()
            .map(
                contribution ->
                    RiskFactorResult.builder()
                        .assessment(assessment)
                        .factorKey(contribution.factorKey())
                        .points(contribution.points())
                        .matched(contribution.matched())
                        .explanation(contribution.explanation())
                        .evidence(contribution.evidence())
                        .build())
            .toList());

    try {
      RiskAssessment saved = riskAssessmentRepository.saveAndFlush(assessment);
      meterRegistry.counter("noscam.risk.assessments").increment();
      maybeAlert(saved, settings, alertType);
      return saved;
    } catch (DataIntegrityViolationException ex) {
      return riskAssessmentRepository
          .findByTransactionAndContentHash(transaction, contentHash)
          .orElseThrow(() -> ex);
    }
  }

  private void maybeAlert(
      RiskAssessment assessment, RiskSettings settings, NotificationType alertType) {
    if (assessment.getScore() < settings.getAlertThreshold()) {
      return;
    }
    Transaction transaction = assessment.getTransaction();
    if (transaction.getStatus() != TransactionStatus.ACTIVE) {
      return;
    }
    Instant posted = transaction.effectiveDate();
    if (alertType == NotificationType.SETTINGS_RESCORE
        && posted.isBefore(Instant.now().minus(ALERT_LOOKBACK))) {
      return;
    }
    notificationService.createAlert(assessment, alertType);
  }

  private Map<String, Object> snapshot(RiskSettings settings) {
    Map<String, Object> snapshot = new LinkedHashMap<>();
    snapshot.put("alertThreshold", settings.getAlertThreshold());
    snapshot.put("lowMax", settings.getLowMax());
    snapshot.put("mediumMax", settings.getMediumMax());
    snapshot.put("configVersion", settings.getConfigVersion());
    snapshot.put("engineVersion", settings.getEngineVersion());
    snapshot.put(
        "factors",
        settings.getFactorConfigs().stream()
            .map(
                config ->
                    Map.of(
                        "key",
                        config.getFactorKey(),
                        "enabled",
                        config.isEnabled(),
                        "maxPoints",
                        config.getMaxPoints(),
                        "parameters",
                        config.getParameters()))
            .toList());
    return snapshot;
  }
}
