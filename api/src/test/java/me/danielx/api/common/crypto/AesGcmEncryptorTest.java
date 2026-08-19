package me.danielx.api.common.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AesGcmEncryptorTest {
  @Test
  void roundTripsAccessTokens() {
    AesGcmEncryptor encryptor =
        new AesGcmEncryptor("MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=");
    String encrypted = encryptor.encrypt("access-sandbox-secret");
    assertTrue(encrypted.startsWith("v1:"));
    assertEquals("access-sandbox-secret", encryptor.decrypt(encrypted));
  }
}
