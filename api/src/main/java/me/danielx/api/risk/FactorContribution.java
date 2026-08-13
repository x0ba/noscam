package me.danielx.api.risk;

import java.util.Map;

public record FactorContribution(
    String factorKey,
    int points,
    boolean matched,
    String explanation,
    Map<String, Object> evidence) {

  public static FactorContribution none(String factorKey, String explanation) {
    return new FactorContribution(factorKey, 0, false, explanation, Map.of());
  }
}
