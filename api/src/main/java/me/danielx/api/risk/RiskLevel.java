package me.danielx.api.risk;

public enum RiskLevel {
  LOW,
  MEDIUM,
  HIGH;

  public static RiskLevel fromScore(int score, int lowMax, int mediumMax) {
    if (score <= lowMax) {
      return LOW;
    }
    if (score <= mediumMax) {
      return MEDIUM;
    }
    return HIGH;
  }
}
