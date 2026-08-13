package me.danielx.api.risk;

public class InvalidRiskSettingsException extends RuntimeException {
  public InvalidRiskSettingsException(String message) {
    super(message);
  }
}
