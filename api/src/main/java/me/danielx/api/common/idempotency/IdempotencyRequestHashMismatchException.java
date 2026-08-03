package me.danielx.api.common.idempotency;

public class IdempotencyRequestHashMismatchException extends RuntimeException {
  public IdempotencyRequestHashMismatchException() {
    super("The request hash does not match the stored hash.");
  }
}
