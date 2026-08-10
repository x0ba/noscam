package me.danielx.api.transactions;

import me.danielx.api.common.accounts.Account;
import me.danielx.api.users.User;

import java.math.BigDecimal;
import java.time.Instant;

public record IngestedTransaction(
    Account account,
    User user,
    SourceType sourceType,
    String externalId,
    BigDecimal amount,
    String currencyCode,
    String merchant,
    String displayName,
    String originalDescription,
    Instant authorizedAt,
    Instant postedAt,
    boolean pending,
    String pendingTransactionId,
    String category,
    String paymentChannel,
    String merchantCountry,
    TransactionStatus status) {}
