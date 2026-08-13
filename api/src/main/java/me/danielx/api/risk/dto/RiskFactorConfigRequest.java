package me.danielx.api.risk.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record RiskFactorConfigRequest(
    @NotBlank String key,
    boolean enabled,
    @Min(0) int maxPoints,
    Map<String, Object> parameters) {}
