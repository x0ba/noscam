package me.danielx.api.plaid;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.SignedJWT;
import me.danielx.api.common.utils.Sha256Util;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlaidWebhookVerifier {
  private static final long MAX_AGE_SECONDS = 300;

  private final PlaidProperties properties;
  private final PlaidClient plaidClient;
  private final Map<String, PlaidWebhookKey> keyCache = new ConcurrentHashMap<>();

  public PlaidWebhookVerifier(PlaidProperties properties, PlaidClient plaidClient) {
    this.properties = properties;
    this.plaidClient = plaidClient;
  }

  public void verify(String jwt, String rawBody) {
    if (!properties.webhookVerificationEnabled()) {
      return;
    }
    if (jwt == null || jwt.isBlank()) {
      throw new InvalidPlaidWebhookException("Missing Plaid-Verification header");
    }
    try {
      SignedJWT signed = SignedJWT.parse(jwt);
      JWSHeader header = signed.getHeader();
      if (header.getAlgorithm() != JWSAlgorithm.ES256 || header.getKeyID() == null) {
        throw new InvalidPlaidWebhookException("Unsupported webhook signature");
      }
      PlaidWebhookKey key =
          keyCache.computeIfAbsent(header.getKeyID(), plaidClient::fetchWebhookKey);
      ECKey ecKey =
          new ECKey.Builder(Curve.P_256, new Base64URL(key.x()), new Base64URL(key.y())).build();
      if (!signed.verify(new ECDSAVerifier(ecKey))) {
        throw new InvalidPlaidWebhookException("Webhook signature is invalid");
      }
      Instant issuedAt = signed.getJWTClaimsSet().getIssueTime().toInstant();
      if (issuedAt.isBefore(Instant.now().minusSeconds(MAX_AGE_SECONDS))) {
        throw new InvalidPlaidWebhookException("Webhook token is too old");
      }
      String expectedHash = String.valueOf(signed.getJWTClaimsSet().getClaim("request_body_sha256"));
      String actualHash = Sha256Util.sha256(rawBody);
      if (!expectedHash.equalsIgnoreCase(actualHash)) {
        throw new InvalidPlaidWebhookException("Webhook body hash does not match");
      }
    } catch (InvalidPlaidWebhookException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new InvalidPlaidWebhookException("Webhook verification failed");
    }
  }
}
