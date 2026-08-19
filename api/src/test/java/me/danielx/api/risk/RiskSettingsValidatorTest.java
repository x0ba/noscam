package me.danielx.api.risk;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RiskSettingsValidatorTest {
  private final RiskSettingsValidator validator = new RiskSettingsValidator();

  @Test
  void rejectsUnknownFactorKeys() {
    assertThrows(
        InvalidRiskSettingsException.class,
        () ->
            validator.validate(
                70,
                39,
                69,
                List.of(new RiskSettingsValidator.FactorDraft("not_a_factor", true, 10, Map.of()))));
  }

  @Test
  void rejectsUnorderedBoundaries() {
    assertThrows(
        InvalidRiskSettingsException.class,
        () ->
            validator.validate(
                70,
                80,
                20,
                List.of(
                    new RiskSettingsValidator.FactorDraft(
                        RiskFactorKeys.NEW_MERCHANT, true, 10, Map.of()))));
  }

  @Test
  void acceptsDocumentedDefaults() {
    assertDoesNotThrow(
        () ->
            validator.validate(
                70,
                39,
                69,
                RiskFactorKeys.ALL.stream()
                    .map(key -> new RiskSettingsValidator.FactorDraft(key, true, 10, Map.of()))
                    .toList()));
  }
}
