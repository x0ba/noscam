package me.danielx.api.accounts.dto;

import jakarta.validation.constraints.NotNull;
import me.danielx.api.accounts.Account;
import me.danielx.api.accounts.AccountType;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
    @NotNull UUID id,
    @NotNull @Length(max = 150) String bank,
    @NotNull @Length(max = 255) String accountName,
    @NotNull AccountType accountType,
    @NotNull @Length(max = 3) String currency,
    @NotNull Instant createdAt) {

  public static AccountResponse fromAccount(Account account) {
    return new AccountResponse(
        account.getPublicId(),
        account.getBank(),
        account.getAccountName(),
        account.getAccountType(),
        account.getCurrency(),
        account.getCreatedAt());
  }
}
