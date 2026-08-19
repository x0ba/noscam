package me.danielx.api.support;

import me.danielx.api.plaid.PlaidClient;
import me.danielx.api.plaid.PlaidModels;
import me.danielx.api.plaid.PlaidWebhookKey;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@Profile("test")
public class FakePlaidClient implements PlaidClient {
  @Override
  public PlaidModels.LinkTokenResult createLinkToken(String clientUserId, String webhookUrl) {
    return new PlaidModels.LinkTokenResult("link-sandbox-test", "2030-01-01T00:00:00Z");
  }

  @Override
  public PlaidModels.ExchangeResult exchangePublicToken(String publicToken) {
    return new PlaidModels.ExchangeResult(
        "access-sandbox-test", "item-sandbox-test", "ins_1", "Chase");
  }

  @Override
  public List<PlaidModels.PlaidAccount> getAccounts(String accessToken) {
    return List.of(
        new PlaidModels.PlaidAccount(
            "plaid-acc-1",
            "Everyday Checking",
            "Chase Checking",
            "1234",
            "depository",
            "checking",
            new BigDecimal("2840.12"),
            "USD"));
  }

  @Override
  public PlaidModels.SyncPage syncTransactions(String accessToken, String cursor) {
    if (cursor != null && !cursor.isBlank()) {
      return new PlaidModels.SyncPage(List.of(), List.of(), List.of(), cursor, false);
    }
    return new PlaidModels.SyncPage(
        List.of(
            new PlaidModels.PlaidTransaction(
                "txn-customs",
                "plaid-acc-1",
                null,
                new BigDecimal("1.99"),
                "USD",
                "CUSTOMS FEE *PKGHOLD",
                "CUSTOMS FEE *PKGHOLD",
                "CUSTOMS FEE *PKGHOLD",
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 7),
                false,
                "other",
                List.of("Service"),
                "US")),
        List.of(),
        List.of(),
        "cursor-1",
        false);
  }

  @Override
  public void removeItem(String accessToken) {}

  @Override
  public PlaidWebhookKey fetchWebhookKey(String keyId) {
    throw new UnsupportedOperationException("Verification is disabled in tests");
  }
}
