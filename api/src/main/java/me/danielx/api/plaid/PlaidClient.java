package me.danielx.api.plaid;

import java.util.List;

public interface PlaidClient {
  PlaidModels.LinkTokenResult createLinkToken(String clientUserId, String webhookUrl);

  PlaidModels.ExchangeResult exchangePublicToken(String publicToken);

  List<PlaidModels.PlaidAccount> getAccounts(String accessToken);

  PlaidModels.SyncPage syncTransactions(String accessToken, String cursor);

  void removeItem(String accessToken);

  PlaidWebhookKey fetchWebhookKey(String keyId);
}
