package me.danielx.api.transactions;

public class TransactionCreationException extends RuntimeException {
  public TransactionCreationException(Throwable cause) {
    super("Transaction could not be created", cause);
  }
}
