package me.danielx.api.risk;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class ConfigNumbers {
  private ConfigNumbers() {}

  public static int intValue(Map<String, Object> params, String key, int fallback) {
    Object value = params.get(key);
    if (value instanceof Number number) {
      return number.intValue();
    }
    if (value instanceof String text) {
      return Integer.parseInt(text);
    }
    return fallback;
  }

  public static BigDecimal decimal(Map<String, Object> params, String key, String fallback) {
    Object value = params.get(key);
    if (value instanceof Number number) {
      return new BigDecimal(number.toString());
    }
    if (value instanceof String text) {
      return new BigDecimal(text);
    }
    return new BigDecimal(fallback);
  }

  public static List<String> strings(Map<String, Object> params, String key, List<String> fallback) {
    Object value = params.get(key);
    if (value instanceof Collection<?> collection) {
      return collection.stream().map(String::valueOf).toList();
    }
    return fallback;
  }
}
