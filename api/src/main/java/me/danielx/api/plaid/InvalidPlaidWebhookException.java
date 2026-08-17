package me.danielx.api.plaid;

public class InvalidPlaidWebhookException extends RuntimeException {
  public InvalidPlaidWebhookException(String message) {
    super(message);
  }
}
