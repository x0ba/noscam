package me.danielx.api.risk;

import me.danielx.api.transactions.Transaction;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RiskEngine {
  public static final int ENGINE_VERSION = 1;

  private final Map<String, RiskFactor> factorsByKey;

  public RiskEngine(List<RiskFactor> factors) {
    this.factorsByKey =
        factors.stream().collect(Collectors.toMap(RiskFactor::key, Function.identity()));
  }

  public ScoredResult score(
      Transaction transaction,
      List<Transaction> accountHistory,
      List<Transaction> userHistory,
      RiskSettings settings) {
    List<FactorContribution> contributions =
        settings.getFactorConfigs().stream()
            .sorted(Comparator.comparing(RiskFactorConfig::getFactorKey))
            .map(
                config -> {
                  if (!config.isEnabled()) {
                    return FactorContribution.none(
                        config.getFactorKey(), "Factor is disabled in the current settings.");
                  }
                  RiskFactor factor = factorsByKey.get(config.getFactorKey());
                  if (factor == null) {
                    return FactorContribution.none(
                        config.getFactorKey(), "Unknown factor was ignored.");
                  }
                  return factor.evaluate(
                      new RiskFactorContext(transaction, accountHistory, userHistory, config));
                })
            .toList();

    int raw = contributions.stream().mapToInt(FactorContribution::points).sum();
    int score = Math.max(0, Math.min(100, raw));
    RiskLevel level = RiskLevel.fromScore(score, settings.getLowMax(), settings.getMediumMax());
    String reason =
        contributions.stream()
            .filter(FactorContribution::matched)
            .max(Comparator.comparingInt(contribution -> Math.abs(contribution.points())))
            .map(FactorContribution::explanation)
            .orElse("No unusual patterns were detected.");
    return new ScoredResult(score, level, reason, contributions);
  }

  public record ScoredResult(
      int score, RiskLevel level, String primaryReason, List<FactorContribution> contributions) {}
}
