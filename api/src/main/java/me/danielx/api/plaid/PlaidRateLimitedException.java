package me.danielx.api.plaid;

public class PlaidRateLimitedException extends RuntimeException {
  private final int retryAfterSeconds;

  public PlaidRateLimitedException(int retryAfterSeconds) {
    super("Too many Plaid sync requests. Try again shortly.");
    this.retryAfterSeconds = retryAfterSeconds;
  }

  public int retryAfterSeconds() {
    return retryAfterSeconds;
  }
}
