package me.danielx.api.transactions;

public class TransactionAlreadyExistsException extends RuntimeException {
  public TransactionAlreadyExistsException(String message) {
    super(message);
  }
}
