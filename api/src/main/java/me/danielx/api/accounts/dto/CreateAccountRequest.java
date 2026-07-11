package me.danielx.api.accounts.dto;

import me.danielx.api.accounts.AccountType;
import org.hibernate.validator.constraints.Length;

public record CreateAccountRequest(
    @Length(min = 1, max = 150) String bank,
    @Length(min = 1, max = 255) String accountName,
    AccountType type,
    @Length(max = 3) String currency) {}
