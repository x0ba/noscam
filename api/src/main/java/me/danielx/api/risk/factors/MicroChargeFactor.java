package me.danielx.api.risk.factors;

import me.danielx.api.risk.ConfigNumbers;
import me.danielx.api.risk.FactorContribution;
import me.danielx.api.risk.RiskFactor;
import me.danielx.api.risk.RiskFactorContext;
import me.danielx.api.risk.RiskFactorKeys;
import me.danielx.api.risk.TextNormalizer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class MicroChargeFactor implements RiskFactor {
  private static final List<String> HINTS =
      List.of("verification", "verify", "customs", "customs fee", "auth", "hold");

  @Override
  public String key() {
    return RiskFactorKeys.MICRO_CHARGE;
  }

  @Override
  public FactorContribution evaluate(RiskFactorContext context) {
    BigDecimal min = ConfigNumbers.decimal(context.parameters(), "minAmount", "0.50");
    BigDecimal max = ConfigNumbers.decimal(context.parameters(), "maxAmount", "3.00");
    BigDecimal outflow = context.transaction().getAmount().abs();
    boolean isOutflow = context.transaction().getAmount().signum() < 0;
    boolean inRange = isOutflow && outflow.compareTo(min) >= 0 && outflow.compareTo(max) <= 0;
    if (!inRange) {
      return FactorContribution.none(key(), "Amount is outside the micro-charge range.");
    }
    String text =
        String.join(
            " ",
            nullToEmpty(context.transaction().getMerchant()),
            nullToEmpty(context.transaction().getOriginalDescription()));
    boolean hinted = HINTS.stream().anyMatch(hint -> TextNormalizer.containsPhrase(text, hint));
    int points = hinted ? context.maxPoints() : Math.max(1, context.maxPoints() / 2);
    return new FactorContribution(
        key(),
        points,
        true,
        hinted
            ? "Small verification-like charge in a typical scam range."
            : "Tiny outbound charge that is often used to test a card.",
        Map.of("amount", outflow, "verificationHint", hinted));
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }
}
