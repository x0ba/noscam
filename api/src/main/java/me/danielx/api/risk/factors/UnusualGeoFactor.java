package me.danielx.api.risk.factors;

import me.danielx.api.risk.FactorContribution;
import me.danielx.api.risk.RiskFactor;
import me.danielx.api.risk.RiskFactorContext;
import me.danielx.api.risk.RiskFactorKeys;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UnusualGeoFactor implements RiskFactor {
  @Override
  public String key() {
    return RiskFactorKeys.UNUSUAL_GEO;
  }

  @Override
  public FactorContribution evaluate(RiskFactorContext context) {
    String country = normalize(context.transaction().getMerchantCountry());
    String currency = normalize(context.transaction().getCurrencyCode());
    Set<String> countries =
        context.accountHistory().stream()
            .filter(txn -> !txn.getPublicId().equals(context.transaction().getPublicId()))
            .map(txn -> normalize(txn.getMerchantCountry()))
            .filter(value -> !value.isBlank())
            .collect(Collectors.toSet());
    Set<String> currencies =
        context.accountHistory().stream()
            .filter(txn -> !txn.getPublicId().equals(context.transaction().getPublicId()))
            .map(txn -> normalize(txn.getCurrencyCode()))
            .filter(value -> !value.isBlank())
            .collect(Collectors.toSet());
    boolean newCountry = !country.isBlank() && !countries.isEmpty() && !countries.contains(country);
    boolean newCurrency = !currency.isBlank() && !currencies.isEmpty() && !currencies.contains(currency);
    if (!newCountry && !newCurrency) {
      return FactorContribution.none(key(), "Country and currency match recent account activity.");
    }
    String reason =
        newCountry && newCurrency
            ? "Charge uses a new country and currency compared with this account."
            : newCountry
                ? "Charge is from a country that has not appeared on this account."
                : "Charge uses a currency that has not appeared on this account.";
    return new FactorContribution(
        key(),
        context.maxPoints(),
        true,
        reason,
        Map.of("country", Objects.requireNonNullElse(country, ""), "currency", currency));
  }

  private static String normalize(String value) {
    return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
  }
}
