package me.danielx.api.risk.factors;

import me.danielx.api.risk.ConfigNumbers;
import me.danielx.api.risk.FactorContribution;
import me.danielx.api.risk.RiskFactor;
import me.danielx.api.risk.RiskFactorContext;
import me.danielx.api.risk.RiskFactorKeys;
import me.danielx.api.risk.TextNormalizer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MerchantKeywordFactor implements RiskFactor {
  public static final List<String> DEFAULT_PHRASES =
      List.of(
          "verification",
          "verify",
          "gift card",
          "crypto",
          "bitcoin",
          "wire",
          "customs fee",
          "customs",
          "refund",
          "support",
          "urgent",
          "immediately",
          "account locked",
          "confirm");

  @Override
  public String key() {
    return RiskFactorKeys.MERCHANT_KEYWORDS;
  }

  @Override
  public FactorContribution evaluate(RiskFactorContext context) {
    String text =
        String.join(
            " ",
            nullToEmpty(context.transaction().getMerchant()),
            nullToEmpty(context.transaction().getDisplayName()),
            nullToEmpty(context.transaction().getOriginalDescription()));
    List<String> phrases =
        ConfigNumbers.strings(context.parameters(), "phrases", DEFAULT_PHRASES);
    List<String> matches =
        phrases.stream().filter(phrase -> TextNormalizer.containsPhrase(text, phrase)).toList();
    if (matches.isEmpty()) {
      return FactorContribution.none(key(), "No suspicious keywords in the merchant text.");
    }
    return new FactorContribution(
        key(),
        context.maxPoints(),
        true,
        "Merchant text includes suspicious language: " + String.join(", ", matches) + ".",
        Map.of("matchedPhrases", matches));
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }
}
