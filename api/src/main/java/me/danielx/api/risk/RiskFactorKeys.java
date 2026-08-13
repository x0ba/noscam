package me.danielx.api.risk;

import java.util.Set;

public final class RiskFactorKeys {
  public static final String MERCHANT_KEYWORDS = "merchant_keywords";
  public static final String MICRO_CHARGE = "micro_charge";
  public static final String LARGE_AMOUNT = "large_amount";
  public static final String NEW_MERCHANT = "new_merchant";
  public static final String UNUSUAL_GEO = "unusual_geo";
  public static final String RISKY_RAIL = "risky_rail";
  public static final String VELOCITY = "velocity";
  public static final String RECURRING_MERCHANT = "recurring_merchant";

  public static final Set<String> ALL =
      Set.of(
          MERCHANT_KEYWORDS,
          MICRO_CHARGE,
          LARGE_AMOUNT,
          NEW_MERCHANT,
          UNUSUAL_GEO,
          RISKY_RAIL,
          VELOCITY,
          RECURRING_MERCHANT);

  private RiskFactorKeys() {}
}
