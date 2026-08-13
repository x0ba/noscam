package me.danielx.api.risk.factors;

import me.danielx.api.risk.ConfigNumbers;
import me.danielx.api.risk.FactorContribution;
import me.danielx.api.risk.RiskFactor;
import me.danielx.api.risk.RiskFactorContext;
import me.danielx.api.risk.RiskFactorKeys;
import me.danielx.api.risk.TextNormalizer;
import me.danielx.api.transactions.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class RecurringMerchantFactor implements RiskFactor {
  @Override
  public String key() {
    return RiskFactorKeys.RECURRING_MERCHANT;
  }

  @Override
  public FactorContribution evaluate(RiskFactorContext context) {
    String merchant = TextNormalizer.normalize(context.transaction().effectiveMerchant());
    if (merchant.isBlank()) {
      return FactorContribution.none(key(), "No merchant to match against recurring history.");
    }
    BigDecimal band = ConfigNumbers.decimal(context.parameters(), "amountBand", "5.00");
    int minOccurrences = ConfigNumbers.intValue(context.parameters(), "minOccurrences", 3);
    List<Transaction> matches =
        context.accountHistory().stream()
            .filter(txn -> !txn.getPublicId().equals(context.transaction().getPublicId()))
            .filter(txn -> TextNormalizer.normalize(txn.effectiveMerchant()).equals(merchant))
            .filter(
                txn ->
                    txn.getAmount()
                            .subtract(context.transaction().getAmount())
                            .abs()
                            .compareTo(band)
                        <= 0)
            .sorted(Comparator.comparing(Transaction::effectiveDate))
            .toList();
    if (matches.size() < minOccurrences - 1) {
      return FactorContribution.none(key(), "Not enough similar charges to treat this as recurring.");
    }
    boolean cadenced = hasMonthlyCadence(matches, context.transaction());
    if (!cadenced) {
      return FactorContribution.none(key(), "Similar charges exist but they do not follow a cadence.");
    }
    int reduction = -Math.abs(context.maxPoints());
    return new FactorContribution(
        key(),
        reduction,
        true,
        "Looks like a recurring charge from a known merchant.",
        Map.of("occurrences", matches.size() + 1));
  }

  private static boolean hasMonthlyCadence(List<Transaction> matches, Transaction current) {
    if (matches.isEmpty()) {
      return false;
    }
    long days =
        Duration.between(matches.getLast().effectiveDate(), current.effectiveDate()).toDays();
    return days >= 20 && days <= 40;
  }
}
