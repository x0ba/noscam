package me.danielx.api.risk.factors;

import me.danielx.api.risk.ConfigNumbers;
import me.danielx.api.risk.FactorContribution;
import me.danielx.api.risk.RiskFactor;
import me.danielx.api.risk.RiskFactorContext;
import me.danielx.api.risk.RiskFactorKeys;
import me.danielx.api.transactions.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class LargeAmountFactor implements RiskFactor {
  @Override
  public String key() {
    return RiskFactorKeys.LARGE_AMOUNT;
  }

  @Override
  public FactorContribution evaluate(RiskFactorContext context) {
    int minHistory = ConfigNumbers.intValue(context.parameters(), "minHistory", 8);
    BigDecimal multiplier = ConfigNumbers.decimal(context.parameters(), "medianMultiplier", "4.0");
    List<BigDecimal> history =
        context.accountHistory().stream()
            .filter(txn -> !txn.getPublicId().equals(context.transaction().getPublicId()))
            .map(Transaction::getAmount)
            .map(BigDecimal::abs)
            .sorted()
            .toList();
    if (history.size() < minHistory) {
      return FactorContribution.none(key(), "Not enough account history to judge an unusual amount.");
    }
    BigDecimal median = percentile(history, 50);
    BigDecimal mad = meanAbsoluteDeviation(history, median);
    BigDecimal amount = context.transaction().getAmount().abs();
    BigDecimal threshold = median.multiply(multiplier);
    boolean large = amount.compareTo(threshold) >= 0 && amount.compareTo(median.add(mad.multiply(BigDecimal.valueOf(3)))) >= 0;
    if (!large) {
      return FactorContribution.none(key(), "Amount is in line with recent account history.");
    }
    return new FactorContribution(
        key(),
        context.maxPoints(),
        true,
        "Amount is much larger than the typical charge on this account.",
        Map.of("median", median, "amount", amount, "threshold", threshold));
  }

  static BigDecimal percentile(List<BigDecimal> sorted, int percentile) {
    if (sorted.isEmpty()) {
      return BigDecimal.ZERO;
    }
    int index = Math.min(sorted.size() - 1, (sorted.size() * percentile) / 100);
    return sorted.get(index);
  }

  static BigDecimal meanAbsoluteDeviation(List<BigDecimal> values, BigDecimal median) {
    if (values.isEmpty()) {
      return BigDecimal.ZERO;
    }
    List<BigDecimal> deviations = new ArrayList<>();
    for (BigDecimal value : values) {
      deviations.add(value.subtract(median).abs());
    }
    Collections.sort(deviations);
    return percentile(deviations, 50).setScale(4, RoundingMode.HALF_UP);
  }
}
