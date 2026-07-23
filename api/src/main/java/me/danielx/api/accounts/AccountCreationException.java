package me.danielx.api.accounts;

public class AccountCreationException extends RuntimeException {
  public AccountCreationException(Throwable cause) {
    super("Account could not be created", cause);
  }
}
