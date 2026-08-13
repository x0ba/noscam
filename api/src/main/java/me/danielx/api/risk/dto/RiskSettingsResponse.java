package me.danielx.api.risk.dto;

import me.danielx.api.risk.RiskEngine;
import me.danielx.api.risk.RiskSettings;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public record RiskSettingsResponse(
    int alertThreshold,
    int lowMax,
    int mediumMax,
    int configVersion,
    int engineVersion,
    List<FactorResponse> factors,
    Instant updatedAt) {

  public static RiskSettingsResponse from(RiskSettings settings) {
    return new RiskSettingsResponse(
        settings.getAlertThreshold(),
        settings.getLowMax(),
        settings.getMediumMax(),
        settings.getConfigVersion(),
        settings.getEngineVersion() == 0 ? RiskEngine.ENGINE_VERSION : settings.getEngineVersion(),
        settings.getFactorConfigs().stream()
            .sorted(Comparator.comparing(config -> config.getFactorKey()))
            .map(
                config ->
                    new FactorResponse(
                        config.getFactorKey(),
                        config.isEnabled(),
                        config.getMaxPoints(),
                        config.getParameters()))
            .toList(),
        settings.getUpdatedAt());
  }

  public record FactorResponse(
      String key, boolean enabled, int maxPoints, Map<String, Object> parameters) {}
}
