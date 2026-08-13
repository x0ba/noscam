package me.danielx.api.risk.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateRiskSettingsRequest(
    @Min(0) @Max(100) int alertThreshold,
    @Min(0) @Max(100) int lowMax,
    @Min(0) @Max(100) int mediumMax,
    @NotEmpty @Valid List<@NotNull RiskFactorConfigRequest> factors) {}
