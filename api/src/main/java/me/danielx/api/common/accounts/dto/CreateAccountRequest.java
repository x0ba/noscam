package me.danielx.api.common.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.danielx.api.common.accounts.AccountType;
import org.hibernate.validator.constraints.Length;

public record CreateAccountRequest(
    @NotBlank @Length(min = 1, max = 150) String bank,
    @NotBlank @Length(min = 1, max = 255) String accountName,
    @NotNull AccountType type,
    @NotBlank @Length(min = 3, max = 3) String currency) {}
