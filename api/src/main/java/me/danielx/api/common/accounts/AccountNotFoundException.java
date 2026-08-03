package me.danielx.api.common.accounts;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {
  public AccountNotFoundException(UUID accountId) {
    super("Account not found: " + accountId);
  }
}
