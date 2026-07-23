package me.danielx.api.users;

public class AuthenticatedUserNotFoundException extends RuntimeException {
  public AuthenticatedUserNotFoundException() {
    super("The authenticated user no longer exists");
  }
}
