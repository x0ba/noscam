package me.danielx.api.accounts.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountListResponse(UUID id, String bank, String accountName, BigDecimal balance) {}
