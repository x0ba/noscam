package me.danielx.api.common.accounts.dto;

import me.danielx.api.common.accounts.Account;
import me.danielx.api.common.accounts.AccountSourceType;
import me.danielx.api.common.accounts.AccountStatus;
import me.danielx.api.common.accounts.AccountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
    UUID id,
    String bank,
    String accountName,
    AccountType accountType,
    BigDecimal balance,
    String currency,
    AccountSourceType sourceType,
    AccountStatus status,
    String mask,
    Instant createdAt) {

  public static AccountResponse fromAccount(Account account) {
    return new AccountResponse(
        account.getPublicId(),
        account.getBank(),
        account.getAccountName(),
        account.getAccountType(),
        account.getBalance(),
        account.getCurrency(),
        account.getSourceType(),
        account.getStatus(),
        account.getMask(),
        account.getCreatedAt());
  }
}
