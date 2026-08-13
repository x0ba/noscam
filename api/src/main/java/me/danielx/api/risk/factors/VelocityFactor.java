package me.danielx.api.risk.factors;

import me.danielx.api.risk.ConfigNumbers;
import me.danielx.api.risk.FactorContribution;
import me.danielx.api.risk.RiskFactor;
import me.danielx.api.risk.RiskFactorContext;
import me.danielx.api.risk.RiskFactorKeys;
import me.danielx.api.transactions.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class VelocityFactor implements RiskFactor {
  @Override
  public String key() {
    return RiskFactorKeys.VELOCITY;
  }

  @Override
  public FactorContribution evaluate(RiskFactorContext context) {
    int windowHours = ConfigNumbers.intValue(context.parameters(), "windowHours", 24);
    int maxCount = ConfigNumbers.intValue(context.parameters(), "maxCount", 6);
    Instant now = context.transaction().effectiveDate();
    Instant windowStart = now.minus(Duration.ofHours(windowHours));
    BigDecimal amount = context.transaction().getAmount();
    List<Transaction> recent =
        context.userHistory().stream()
            .filter(txn -> !txn.getPublicId().equals(context.transaction().getPublicId()))
            .filter(txn -> !txn.effectiveDate().isBefore(windowStart))
            .toList();
    long sameAmount =
        recent.stream().filter(txn -> txn.getAmount().compareTo(amount) == 0).count();
    if (recent.size() + 1 > maxCount) {
      return new FactorContribution(
          key(),
          context.maxPoints(),
          true,
          "Unusually many charges landed in a short window.",
          Map.of("count", recent.size() + 1, "windowHours", windowHours));
    }
    if (sameAmount >= 2) {
      return new FactorContribution(
          key(),
          context.maxPoints(),
          true,
          "The same amount was charged repeatedly in a short window.",
          Map.of("sameAmountCount", sameAmount + 1, "amount", amount));
    }
    return FactorContribution.none(key(), "Charge velocity looks normal.");
  }
}
