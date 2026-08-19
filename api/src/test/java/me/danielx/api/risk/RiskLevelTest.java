package me.danielx.api.risk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskLevelTest {
  @Test
  void derivesPresentationLabels() {
    assertEquals(RiskLevel.LOW, RiskLevel.fromScore(0, 39, 69));
    assertEquals(RiskLevel.LOW, RiskLevel.fromScore(39, 39, 69));
    assertEquals(RiskLevel.MEDIUM, RiskLevel.fromScore(40, 39, 69));
    assertEquals(RiskLevel.MEDIUM, RiskLevel.fromScore(69, 39, 69));
    assertEquals(RiskLevel.HIGH, RiskLevel.fromScore(70, 39, 69));
    assertEquals(RiskLevel.HIGH, RiskLevel.fromScore(100, 39, 69));
  }
}
