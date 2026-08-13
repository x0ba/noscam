package me.danielx.api.risk;

import me.danielx.api.risk.factors.MerchantKeywordFactor;
import me.danielx.api.risk.factors.RiskyRailFactor;
import me.danielx.api.users.User;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DefaultRiskSettingsFactory {
  public RiskSettings create(User user) {
    RiskSettings settings =
        RiskSettings.builder()
            .user(user)
            .alertThreshold(70)
            .lowMax(39)
            .mediumMax(69)
            .configVersion(1)
            .engineVersion(RiskEngine.ENGINE_VERSION)
            .build();
    settings.replaceFactorConfigs(
        List.of(
            config(RiskFactorKeys.MERCHANT_KEYWORDS, true, 35, Map.of("phrases", MerchantKeywordFactor.DEFAULT_PHRASES)),
            config(RiskFactorKeys.MICRO_CHARGE, true, 25, Map.of("minAmount", 0.50, "maxAmount", 3.00)),
            config(RiskFactorKeys.LARGE_AMOUNT, true, 20, Map.of("minHistory", 8, "medianMultiplier", 4.0)),
            config(RiskFactorKeys.NEW_MERCHANT, true, 25, Map.of()),
            config(RiskFactorKeys.UNUSUAL_GEO, true, 15, Map.of()),
            config(
                RiskFactorKeys.RISKY_RAIL,
                true,
                25,
                Map.of(
                    "channels", RiskyRailFactor.DEFAULT_CHANNELS,
                    "categories", RiskyRailFactor.DEFAULT_CATEGORIES)),
            config(RiskFactorKeys.VELOCITY, true, 15, Map.of("windowHours", 24, "maxCount", 6)),
            config(
                RiskFactorKeys.RECURRING_MERCHANT,
                true,
                20,
                Map.of("amountBand", 5.00, "minOccurrences", 3))));
    return settings;
  }

  private static RiskFactorConfig config(
      String key, boolean enabled, int maxPoints, Map<String, Object> parameters) {
    return RiskFactorConfig.builder()
        .factorKey(key)
        .enabled(enabled)
        .maxPoints(maxPoints)
        .parameters(new LinkedHashMap<>(parameters))
        .build();
  }
}
