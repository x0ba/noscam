package me.danielx.api.auth;

public class UserRegistrationException extends RuntimeException {
  public UserRegistrationException(Throwable cause) {
    super("User could not be registered", cause);
  }
}
