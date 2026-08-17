package me.danielx.api.plaid;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "plaid")
public class PlaidProperties {
  private String clientId = "";
  private String secret = "";
  private String env = "sandbox";
  private String webhookUrl = "";
  private boolean webhookVerificationEnabled = true;

  public String clientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String secret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public String env() {
    return env;
  }

  public void setEnv(String env) {
    this.env = env;
  }

  public String webhookUrl() {
    return webhookUrl;
  }

  public void setWebhookUrl(String webhookUrl) {
    this.webhookUrl = webhookUrl;
  }

  public boolean webhookVerificationEnabled() {
    return webhookVerificationEnabled;
  }

  public void setWebhookVerificationEnabled(boolean webhookVerificationEnabled) {
    this.webhookVerificationEnabled = webhookVerificationEnabled;
  }

  public String baseUrl() {
    return switch (env.toLowerCase()) {
      case "production" -> "https://production.plaid.com";
      case "development" -> "https://development.plaid.com";
      default -> "https://sandbox.plaid.com";
    };
  }
}
