package me.danielx.api.plaid;

public record PlaidWebhookKey(String kid, String alg, String kty, String crv, String x, String y) {}
