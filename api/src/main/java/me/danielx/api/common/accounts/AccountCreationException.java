package me.danielx.api.common.accounts;

public class AccountCreationException extends RuntimeException {
  public AccountCreationException(Throwable cause) {
    super("Account could not be created", cause);
  }
}
