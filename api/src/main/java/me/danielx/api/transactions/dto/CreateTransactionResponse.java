package me.danielx.api.transactions.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import me.danielx.api.accounts.Account;
import me.danielx.api.transactions.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record CreateTransactionResponse(
    @NotNull UUID id,
    @NotNull UUID accountId,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotBlank @Pattern(regexp = "[A-Za-z]{3}") String currencyCode,
    @NotNull Instant createdAt) {}
