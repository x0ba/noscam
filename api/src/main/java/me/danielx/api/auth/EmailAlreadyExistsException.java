package me.danielx.api.auth;

public class EmailAlreadyExistsException extends RuntimeException {
  public EmailAlreadyExistsException() {
    super("Email already exists");
  }
}
