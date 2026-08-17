package me.danielx.api.plaid;

import java.util.UUID;

public class PlaidItemNotFoundException extends RuntimeException {
  public PlaidItemNotFoundException(UUID itemId) {
    super("Plaid item " + itemId + " was not found");
  }
}
