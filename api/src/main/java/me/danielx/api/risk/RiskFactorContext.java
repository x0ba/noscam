package me.danielx.api.risk;

import me.danielx.api.transactions.Transaction;

import java.util.List;
import java.util.Map;

public record RiskFactorContext(
    Transaction transaction,
    List<Transaction> accountHistory,
    List<Transaction> userHistory,
    RiskFactorConfig config) {

  public Map<String, Object> parameters() {
    return config.getParameters() == null ? Map.of() : config.getParameters();
  }

  public int maxPoints() {
    return config.getMaxPoints();
  }

  public boolean enabled() {
    return config.isEnabled();
  }
}
