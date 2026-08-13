package me.danielx.api.risk;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.danielx.api.risk.dto.RiskSettingsResponse;
import me.danielx.api.risk.dto.UpdateRiskSettingsRequest;
import me.danielx.api.users.dto.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/risk-settings")
@Tag(name = "Risk settings", description = "Per-user scoring configuration")
@SecurityRequirement(name = "sessionCookie")
public class RiskSettingsController {
  private final RiskSettingsService riskSettingsService;

  public RiskSettingsController(RiskSettingsService riskSettingsService) {
    this.riskSettingsService = riskSettingsService;
  }

  @GetMapping
  @Operation(summary = "Get the current user's risk settings")
  public ResponseEntity<RiskSettingsResponse> get(
      @AuthenticationPrincipal AuthenticatedUser currentUser) {
    return ResponseEntity.ok(riskSettingsService.getForCurrentUser(currentUser));
  }

  @PutMapping
  @Operation(summary = "Replace the current user's risk settings")
  public ResponseEntity<RiskSettingsResponse> update(
      @AuthenticationPrincipal AuthenticatedUser currentUser,
      @Valid @RequestBody UpdateRiskSettingsRequest request) {
    return ResponseEntity.ok(riskSettingsService.replaceForCurrentUser(currentUser, request));
  }
}
