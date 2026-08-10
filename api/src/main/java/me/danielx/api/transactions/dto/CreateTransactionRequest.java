package me.danielx.api.transactions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateTransactionRequest(
    @NotNull UUID accountId,
    @NotNull BigDecimal amount,
    @NotBlank @Pattern(regexp = "[A-Za-z]{3}") String currency,
    @NotBlank @Length(max = 255) String merchant,
    @Length(max = 255) String displayName,
    String originalDescription,
    Instant authorizedAt,
    Instant postedAt,
    Boolean pending,
    @Length(max = 255) String category,
    @Length(max = 64) String paymentChannel,
    @Length(min = 2, max = 2) String merchantCountry) {}
