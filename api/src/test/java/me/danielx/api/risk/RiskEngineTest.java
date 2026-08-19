package me.danielx.api.risk;

import me.danielx.api.common.accounts.Account;
import me.danielx.api.risk.factors.MerchantKeywordFactor;
import me.danielx.api.risk.factors.MicroChargeFactor;
import me.danielx.api.risk.factors.NewMerchantFactor;
import me.danielx.api.risk.factors.RecurringMerchantFactor;
import me.danielx.api.risk.factors.RiskyRailFactor;
import me.danielx.api.transactions.SourceType;
import me.danielx.api.transactions.Transaction;
import me.danielx.api.users.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiskEngineTest {
  private final RiskEngine engine =
      new RiskEngine(
          List.of(
              new MerchantKeywordFactor(),
              new MicroChargeFactor(),
              new NewMerchantFactor(),
              new RiskyRailFactor(),
              new RecurringMerchantFactor()));

  @Test
  void scoresFrontendHighExample() {
    Transaction transaction =
        txn("CUSTOMS FEE *PKGHOLD", new BigDecimal("-1.99"), "other", null);
    RiskEngine.ScoredResult result =
        engine.score(transaction, List.of(), List.of(), settings(70, 39, 69));
    assertTrue(result.score() >= 70);
    assertEquals(RiskLevel.HIGH, result.level());
    assertTrue(result.primaryReason().toLowerCase().contains("customs")
        || result.primaryReason().toLowerCase().contains("tiny")
        || result.primaryReason().toLowerCase().contains("small"));
  }

  @Test
  void scoresFrontendMediumExample() {
    Transaction transaction = txn("VENMO PAYMENT *ALEXM", new BigDecimal("-38.00"), "p2p", null);
    RiskEngine.ScoredResult result =
        engine.score(transaction, List.of(), List.of(), settings(70, 39, 69));
    assertTrue(result.score() >= 40);
    assertTrue(result.score() < 70);
    assertEquals(RiskLevel.MEDIUM, result.level());
  }

  @Test
  void scoresFrontendLowExample() {
    Transaction grocery = txn("BLUE BOTTLE SF MISSION", new BigDecimal("-6.45"), "in store", null);
    RiskEngine.ScoredResult result =
        engine.score(grocery, List.of(groceryHistory()), List.of(), settings(70, 39, 69));
    assertTrue(result.score() < 40);
    assertEquals(RiskLevel.LOW, result.level());
  }

  @Test
  void clampsToZeroOneHundred() {
    RiskSettings settings = settings(70, 39, 69);
    settings.getFactorConfigs().forEach(config -> config.setMaxPoints(80));
    Transaction transaction = txn("WIRE TO UNKNOWN *INTL CRYPTO", new BigDecimal("-500.00"), "wire", "US");
    RiskEngine.ScoredResult result = engine.score(transaction, List.of(), List.of(), settings);
    assertEquals(100, result.score());
  }

  @Test
  void disabledFactorsContributeNothing() {
    RiskSettings settings = settings(70, 39, 69);
    settings.getFactorConfigs().forEach(config -> config.setEnabled(false));
    Transaction transaction = txn("CUSTOMS FEE *PKGHOLD", new BigDecimal("-1.99"), "other", null);
    RiskEngine.ScoredResult result = engine.score(transaction, List.of(), List.of(), settings);
    assertEquals(0, result.score());
    assertEquals("No unusual patterns were detected.", result.primaryReason());
  }

  private static Transaction groceryHistory() {
    Transaction history = txn("BLUE BOTTLE SF MISSION", new BigDecimal("-6.10"), "in store", null);
    history.setPublicId(UUID.randomUUID());
    history.setPostedAt(Instant.parse("2026-06-01T00:00:00Z"));
    return history;
  }

  private static Transaction txn(String merchant, BigDecimal amount, String channel, String country) {
    User user = User.builder().email("user@example.com").passwordHash("x").firstName("A").lastName("B").build();
    user.setId(1L);
    Account account = Account.builder().user(user).bank("Chase").accountName("Checking").currency("USD").build();
    account.setId(1L);
    account.setPublicId(UUID.randomUUID());
    return Transaction.builder()
        .publicId(UUID.randomUUID())
        .user(user)
        .account(account)
        .sourceType(SourceType.MANUAL)
        .amount(amount)
        .currencyCode("USD")
        .merchant(merchant)
        .paymentChannel(channel)
        .merchantCountry(country)
        .postedAt(Instant.parse("2026-07-08T00:00:00Z"))
        .createdAt(Instant.parse("2026-07-08T00:00:00Z"))
        .build();
  }

  private static RiskSettings settings(int threshold, int lowMax, int mediumMax) {
    RiskSettings settings =
        RiskSettings.builder()
            .alertThreshold(threshold)
            .lowMax(lowMax)
            .mediumMax(mediumMax)
            .configVersion(1)
            .engineVersion(1)
            .build();
    settings.replaceFactorConfigs(
        List.of(
            config(RiskFactorKeys.MERCHANT_KEYWORDS, 35, Map.of("phrases", MerchantKeywordFactor.DEFAULT_PHRASES)),
            config(RiskFactorKeys.MICRO_CHARGE, 25, Map.of("minAmount", 0.50, "maxAmount", 3.00)),
            config(RiskFactorKeys.NEW_MERCHANT, 25, Map.of()),
            config(
                RiskFactorKeys.RISKY_RAIL,
                25,
                Map.of(
                    "channels", RiskyRailFactor.DEFAULT_CHANNELS,
                    "categories", RiskyRailFactor.DEFAULT_CATEGORIES)),
            config(RiskFactorKeys.RECURRING_MERCHANT, 20, Map.of("amountBand", 5.00, "minOccurrences", 3))));
    return settings;
  }

  private static RiskFactorConfig config(String key, int points, Map<String, Object> parameters) {
    return RiskFactorConfig.builder()
        .factorKey(key)
        .enabled(true)
        .maxPoints(points)
        .parameters(parameters)
        .build();
  }
}
