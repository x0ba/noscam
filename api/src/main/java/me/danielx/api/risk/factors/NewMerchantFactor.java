package me.danielx.api.risk.factors;

import me.danielx.api.risk.FactorContribution;
import me.danielx.api.risk.RiskFactor;
import me.danielx.api.risk.RiskFactorContext;
import me.danielx.api.risk.RiskFactorKeys;
import me.danielx.api.risk.TextNormalizer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NewMerchantFactor implements RiskFactor {
  @Override
  public String key() {
    return RiskFactorKeys.NEW_MERCHANT;
  }

  @Override
  public FactorContribution evaluate(RiskFactorContext context) {
    String merchant = TextNormalizer.normalize(context.transaction().effectiveMerchant());
    if (merchant.isBlank()) {
      return FactorContribution.none(key(), "No merchant name to compare against history.");
    }
    boolean seen =
        context.accountHistory().stream()
            .filter(txn -> !txn.getPublicId().equals(context.transaction().getPublicId()))
            .map(txn -> TextNormalizer.normalize(txn.effectiveMerchant()))
            .anyMatch(existing -> existing.equals(merchant));
    if (seen) {
      return FactorContribution.none(key(), "This merchant has appeared on the account before.");
    }
    return new FactorContribution(
        key(),
        context.maxPoints(),
        true,
        "This merchant has not appeared on the account recently.",
        Map.of("merchant", merchant));
  }
}
