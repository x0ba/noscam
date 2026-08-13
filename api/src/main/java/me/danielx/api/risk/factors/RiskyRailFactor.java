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
public class RiskyRailFactor implements RiskFactor {
  public static final List<String> DEFAULT_CHANNELS = List.of("wire", "cash", "p2p", "peer", "crypto");
  public static final List<String> DEFAULT_CATEGORIES =
      List.of("wire", "cash advance", "crypto", "transfer", "venmo", "zelle", "paypal");

  @Override
  public String key() {
    return RiskFactorKeys.RISKY_RAIL;
  }

  @Override
  public FactorContribution evaluate(RiskFactorContext context) {
    List<String> channels =
        ConfigNumbers.strings(context.parameters(), "channels", DEFAULT_CHANNELS);
    List<String> categories =
        ConfigNumbers.strings(context.parameters(), "categories", DEFAULT_CATEGORIES);
    String channel = nullToEmpty(context.transaction().getPaymentChannel());
    String category = nullToEmpty(context.transaction().getCategory());
    String merchant = nullToEmpty(context.transaction().effectiveMerchant());
    String haystack = String.join(" ", channel, category, merchant);
    List<String> matches =
        java.util.stream.Stream.concat(channels.stream(), categories.stream())
            .distinct()
            .filter(token -> TextNormalizer.containsPhrase(haystack, token))
            .toList();
    if (matches.isEmpty()) {
      return FactorContribution.none(key(), "Payment rail and category look ordinary.");
    }
    return new FactorContribution(
        key(),
        context.maxPoints(),
        true,
        "Uses a higher-risk payment rail or category: " + String.join(", ", matches) + ".",
        Map.of("matched", matches));
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }
}
