package me.danielx.api.risk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextNormalizerTest {
  @Test
  void matchesNormalizedPhrases() {
    assertTrue(TextNormalizer.containsPhrase("CUSTOMS FEE *PKGHOLD", "customs fee"));
    assertTrue(TextNormalizer.containsPhrase("AMZN REFUND CONFIRM", "refund"));
    assertFalse(TextNormalizer.containsPhrase("BLUE BOTTLE SF MISSION", "wire"));
  }
}
