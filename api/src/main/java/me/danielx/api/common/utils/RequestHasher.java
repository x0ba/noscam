package me.danielx.api.common.utils;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class RequestHasher {
  private final ObjectMapper objectMapper;

  public RequestHasher(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public String hash(Object fingerprint) {
    try {
      String canonicalJson = objectMapper.writeValueAsString(fingerprint);
      return Sha256Util.sha256(canonicalJson);
    } catch (JacksonException e) {
      throw new IllegalStateException("Failed to serialize fingerprint", e);
    }
  }
}
