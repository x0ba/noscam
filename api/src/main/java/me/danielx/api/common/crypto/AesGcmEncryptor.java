package me.danielx.api.common.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AesGcmEncryptor {
  private static final String PREFIX = "v1:";
  private static final int IV_LENGTH = 12;
  private static final int TAG_LENGTH = 128;

  private final byte[] key;
  private final SecureRandom random = new SecureRandom();

  public AesGcmEncryptor(@Value("${app.encryption-key:}") String encodedKey) {
    this.key = encodedKey == null || encodedKey.isBlank() ? new byte[0] : Base64.getDecoder().decode(encodedKey);
  }

  public String encrypt(String plaintext) {
    requireKey();
    try {
      byte[] iv = new byte[IV_LENGTH];
      random.nextBytes(iv);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_LENGTH, iv));
      byte[] ciphertext = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
      buffer.put(iv);
      buffer.put(ciphertext);
      return PREFIX + Base64.getEncoder().encodeToString(buffer.array());
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to encrypt secret", ex);
    }
  }

  public String decrypt(String ciphertext) {
    requireKey();
    if (ciphertext == null || !ciphertext.startsWith(PREFIX)) {
      throw new IllegalStateException("Encrypted value is missing the expected prefix");
    }
    try {
      byte[] decoded = Base64.getDecoder().decode(ciphertext.substring(PREFIX.length()));
      ByteBuffer buffer = ByteBuffer.wrap(decoded);
      byte[] iv = new byte[IV_LENGTH];
      buffer.get(iv);
      byte[] body = new byte[buffer.remaining()];
      buffer.get(body);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_LENGTH, iv));
      return new String(cipher.doFinal(body), java.nio.charset.StandardCharsets.UTF_8);
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to decrypt secret", ex);
    }
  }

  public boolean hasKey() {
    return key.length == 32;
  }

  private void requireKey() {
    if (!hasKey()) {
      throw new IllegalStateException("APP_ENCRYPTION_KEY must be a 32-byte Base64 value");
    }
  }
}
