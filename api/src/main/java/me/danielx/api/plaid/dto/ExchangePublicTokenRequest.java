package me.danielx.api.plaid.dto;

import jakarta.validation.constraints.NotBlank;

public record ExchangePublicTokenRequest(@NotBlank String publicToken) {}
