package me.danielx.api.plaid;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("!test")
public class HttpPlaidClient implements PlaidClient {
  private final PlaidProperties properties;
  private final RestClient restClient;
  private final ObjectMapper objectMapper;

  public HttpPlaidClient(PlaidProperties properties, RestClient.Builder builder, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.restClient = builder.baseUrl(properties.baseUrl()).build();
  }

  @Override
  public PlaidModels.LinkTokenResult createLinkToken(String clientUserId, String webhookUrl) {
    Map<String, Object> body = authBody();
    body.put("client_name", "Noscam");
    body.put("language", "en");
    body.put("country_codes", List.of("US"));
    body.put("user", Map.of("client_user_id", clientUserId));
    body.put("products", List.of("transactions"));
    if (webhookUrl != null && !webhookUrl.isBlank()) {
      body.put("webhook", webhookUrl);
    }
    JsonNode response = post("/link/token/create", body);
    return new PlaidModels.LinkTokenResult(text(response, "link_token"), text(response, "expiration"));
  }

  @Override
  public PlaidModels.ExchangeResult exchangePublicToken(String publicToken) {
    Map<String, Object> body = authBody();
    body.put("public_token", publicToken);
    JsonNode exchange = post("/item/public_token/exchange", body);
    String accessToken = text(exchange, "access_token");
    String itemId = text(exchange, "item_id");
    Map<String, Object> itemBody = authBody();
    itemBody.put("access_token", accessToken);
    JsonNode item = post("/item/get", itemBody);
    JsonNode institution = item.path("item");
    return new PlaidModels.ExchangeResult(
        accessToken,
        itemId,
        text(institution, "institution_id"),
        null);
  }

  @Override
  public List<PlaidModels.PlaidAccount> getAccounts(String accessToken) {
    Map<String, Object> body = authBody();
    body.put("access_token", accessToken);
    JsonNode response = post("/accounts/get", body);
    List<PlaidModels.PlaidAccount> accounts = new ArrayList<>();
    for (JsonNode node : response.path("accounts")) {
      JsonNode balances = node.path("balances");
      accounts.add(
          new PlaidModels.PlaidAccount(
              text(node, "account_id"),
              text(node, "name"),
              text(node, "official_name"),
              text(node, "mask"),
              text(node, "type"),
              text(node, "subtype"),
              decimal(balances, "current"),
              firstNonBlank(text(balances, "iso_currency_code"), text(balances, "unofficial_currency_code"))));
    }
    return accounts;
  }

  @Override
  public PlaidModels.SyncPage syncTransactions(String accessToken, String cursor) {
    Map<String, Object> body = authBody();
    body.put("access_token", accessToken);
    if (cursor != null && !cursor.isBlank()) {
      body.put("cursor", cursor);
    }
    body.put("count", 500);
    JsonNode response = post("/transactions/sync", body);
    return new PlaidModels.SyncPage(
        readTransactions(response.path("added")),
        readTransactions(response.path("modified")),
        readRemoved(response.path("removed")),
        text(response, "next_cursor"),
        response.path("has_more").asBoolean(false));
  }

  @Override
  public void removeItem(String accessToken) {
    Map<String, Object> body = authBody();
    body.put("access_token", accessToken);
    post("/item/remove", body);
  }

  @Override
  public PlaidWebhookKey fetchWebhookKey(String keyId) {
    Map<String, Object> body = authBody();
    body.put("key_id", keyId);
    JsonNode key = post("/webhook_verification_key/get", body).path("key");
    return new PlaidWebhookKey(
        text(key, "kid"),
        text(key, "alg"),
        text(key, "kty"),
        text(key, "crv"),
        text(key, "x"),
        text(key, "y"));
  }

  private List<PlaidModels.PlaidTransaction> readTransactions(JsonNode array) {
    List<PlaidModels.PlaidTransaction> transactions = new ArrayList<>();
    for (JsonNode node : array) {
      List<String> categories = new ArrayList<>();
      node.path("category").forEach(item -> categories.add(item.asText()));
      JsonNode location = node.path("location");
      transactions.add(
          new PlaidModels.PlaidTransaction(
              text(node, "transaction_id"),
              text(node, "account_id"),
              text(node, "pending_transaction_id"),
              decimal(node, "amount"),
              firstNonBlank(text(node, "iso_currency_code"), text(node, "unofficial_currency_code")),
              text(node, "merchant_name"),
              text(node, "name"),
              text(node, "original_description"),
              date(node, "date"),
              date(node, "authorized_date"),
              node.path("pending").asBoolean(false),
              text(node, "payment_channel"),
              categories,
              text(location, "country")));
    }
    return transactions;
  }

  private List<PlaidModels.PlaidTransaction> readRemoved(JsonNode array) {
    List<PlaidModels.PlaidTransaction> removed = new ArrayList<>();
    for (JsonNode node : array) {
      removed.add(
          new PlaidModels.PlaidTransaction(
              text(node, "transaction_id"),
              text(node, "account_id"),
              null,
              BigDecimal.ZERO,
              "USD",
              null,
              null,
              null,
              null,
              null,
              false,
              null,
              List.of(),
              null));
    }
    return removed;
  }

  private Map<String, Object> authBody() {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("client_id", properties.clientId());
    body.put("secret", properties.secret());
    return body;
  }

  private JsonNode post(String path, Map<String, Object> body) {
    String json = restClient.post()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .body(String.class);
    return objectMapper.readTree(json);
  }

  private static String text(JsonNode node, String field) {
    JsonNode value = node.path(field);
    return value.isMissingNode() || value.isNull() ? null : value.asText();
  }

  private static BigDecimal decimal(JsonNode node, String field) {
    JsonNode value = node.path(field);
    if (value.isMissingNode() || value.isNull()) {
      return BigDecimal.ZERO;
    }
    return new BigDecimal(value.asText());
  }

  private static LocalDate date(JsonNode node, String field) {
    String value = text(node, field);
    return value == null || value.isBlank() ? null : LocalDate.parse(value);
  }

  private static String firstNonBlank(String left, String right) {
    if (left != null && !left.isBlank()) {
      return left;
    }
    return right;
  }
}
