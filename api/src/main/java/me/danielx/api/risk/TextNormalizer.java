package me.danielx.api.risk;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class TextNormalizer {
  private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");

  private TextNormalizer() {}

  public static String normalize(String raw) {
    if (raw == null) {
      return "";
    }
    return NON_ALNUM.matcher(raw.toLowerCase(Locale.ROOT)).replaceAll(" ").trim();
  }

  public static List<String> tokens(String raw) {
    String normalized = normalize(raw);
    if (normalized.isBlank()) {
      return List.of();
    }
    return Arrays.stream(normalized.split(" ")).filter(token -> !token.isBlank()).toList();
  }

  public static boolean containsPhrase(String haystack, String phrase) {
    String normalizedHaystack = " " + normalize(haystack) + " ";
    String normalizedPhrase = " " + normalize(phrase) + " ";
    return !normalizedPhrase.isBlank() && normalizedHaystack.contains(normalizedPhrase);
  }
}
