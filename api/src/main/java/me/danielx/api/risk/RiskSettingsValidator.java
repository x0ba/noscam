package me.danielx.api.risk;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class RiskSettingsValidator {
  public void validate(int alertThreshold, int lowMax, int mediumMax, List<FactorDraft> factors) {
    requireRange("alertThreshold", alertThreshold);
    requireRange("lowMax", lowMax);
    requireRange("mediumMax", mediumMax);
    if (lowMax >= mediumMax) {
      throw new InvalidRiskSettingsException("lowMax must be less than mediumMax");
    }
    if (factors == null || factors.isEmpty()) {
      throw new InvalidRiskSettingsException("At least one factor configuration is required");
    }
    Set<String> seen = new HashSet<>();
    for (FactorDraft factor : factors) {
      if (factor.key() == null || factor.key().isBlank()) {
        throw new InvalidRiskSettingsException("Factor key is required");
      }
      if (!RiskFactorKeys.ALL.contains(factor.key())) {
        throw new InvalidRiskSettingsException("Unknown factor key: " + factor.key());
      }
      if (!seen.add(factor.key())) {
        throw new InvalidRiskSettingsException("Duplicate factor key: " + factor.key());
      }
      if (factor.maxPoints() < 0) {
        throw new InvalidRiskSettingsException("Factor weights must be nonnegative");
      }
      validateParameters(factor.key(), factor.parameters() == null ? Map.of() : factor.parameters());
    }
  }

  private void requireRange(String field, int value) {
    if (value < 0 || value > 100) {
      throw new InvalidRiskSettingsException(field + " must be between 0 and 100");
    }
  }

  private void validateParameters(String key, Map<String, Object> parameters) {
    switch (key) {
      case RiskFactorKeys.MERCHANT_KEYWORDS -> requireStringList(key, parameters, "phrases");
      case RiskFactorKeys.MICRO_CHARGE -> {
        requireNumber(key, parameters, "minAmount");
        requireNumber(key, parameters, "maxAmount");
      }
      case RiskFactorKeys.LARGE_AMOUNT -> {
        requireInteger(key, parameters, "minHistory");
        requireNumber(key, parameters, "medianMultiplier");
      }
      case RiskFactorKeys.NEW_MERCHANT -> {
        // lookback is owned by the scoring service, not factor params
      }
      case RiskFactorKeys.UNUSUAL_GEO -> {
      }
      case RiskFactorKeys.RISKY_RAIL -> {
        requireStringList(key, parameters, "channels");
        requireStringList(key, parameters, "categories");
      }
      case RiskFactorKeys.VELOCITY -> {
        requireInteger(key, parameters, "windowHours");
        requireInteger(key, parameters, "maxCount");
      }
      case RiskFactorKeys.RECURRING_MERCHANT -> {
        requireNumber(key, parameters, "amountBand");
        requireInteger(key, parameters, "minOccurrences");
      }
      default -> throw new InvalidRiskSettingsException("Unknown factor key: " + key);
    }
  }

  private void requireNumber(String factor, Map<String, Object> parameters, String field) {
    Object value = parameters.get(field);
    if (value != null && !(value instanceof Number) && !isNumericString(value)) {
      throw new InvalidRiskSettingsException(factor + "." + field + " must be a number");
    }
  }

  private void requireInteger(String factor, Map<String, Object> parameters, String field) {
    Object value = parameters.get(field);
    if (value != null && !(value instanceof Number) && !isNumericString(value)) {
      throw new InvalidRiskSettingsException(factor + "." + field + " must be an integer");
    }
  }

  private void requireStringList(String factor, Map<String, Object> parameters, String field) {
    Object value = parameters.get(field);
    if (value == null) {
      return;
    }
    if (!(value instanceof List<?> list) || list.stream().anyMatch(item -> !(item instanceof String))) {
      throw new InvalidRiskSettingsException(factor + "." + field + " must be a list of strings");
    }
  }

  private boolean isNumericString(Object value) {
    if (!(value instanceof String text)) {
      return false;
    }
    try {
      Double.parseDouble(text);
      return true;
    } catch (NumberFormatException ex) {
      return false;
    }
  }

  public record FactorDraft(
      String key, boolean enabled, int maxPoints, Map<String, Object> parameters) {}
}
