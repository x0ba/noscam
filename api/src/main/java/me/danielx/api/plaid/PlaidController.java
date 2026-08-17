package me.danielx.api.plaid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.danielx.api.plaid.dto.ExchangePublicTokenRequest;
import me.danielx.api.plaid.dto.LinkTokenResponse;
import me.danielx.api.plaid.dto.PlaidItemResponse;
import me.danielx.api.users.dto.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plaid")
@Tag(name = "Plaid", description = "Bank connections through Plaid Link")
@SecurityRequirement(name = "sessionCookie")
public class PlaidController {
  private final PlaidService plaidService;

  public PlaidController(PlaidService plaidService) {
    this.plaidService = plaidService;
  }

  @PostMapping("/link-token")
  @Operation(summary = "Create a Plaid Link token")
  public ResponseEntity<LinkTokenResponse> createLinkToken(
      @AuthenticationPrincipal AuthenticatedUser currentUser) {
    return ResponseEntity.ok(plaidService.createLinkToken(currentUser));
  }

  @PostMapping("/exchange")
  @Operation(summary = "Exchange a Plaid public token and queue the first sync")
  public ResponseEntity<PlaidItemResponse> exchange(
      @AuthenticationPrincipal AuthenticatedUser currentUser,
      @RequestHeader("Idempotency-Key") String idempotencyKey,
      @Valid @RequestBody ExchangePublicTokenRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(plaidService.exchange(currentUser, request, idempotencyKey));
  }

  @GetMapping("/items")
  @Operation(summary = "List Plaid connections for the current user")
  public ResponseEntity<List<PlaidItemResponse>> list(
      @AuthenticationPrincipal AuthenticatedUser currentUser) {
    return ResponseEntity.ok(plaidService.listItems(currentUser));
  }

  @DeleteMapping("/items/{id}")
  @Operation(summary = "Disconnect a Plaid item and keep transaction history")
  public ResponseEntity<Void> disconnect(
      @AuthenticationPrincipal AuthenticatedUser currentUser, @PathVariable UUID id) {
    plaidService.disconnect(currentUser, id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/items/{id}/sync")
  @Operation(summary = "Manually enqueue a Plaid transaction sync")
  public ResponseEntity<Void> sync(
      @AuthenticationPrincipal AuthenticatedUser currentUser, @PathVariable UUID id) {
    plaidService.requestSync(currentUser, id);
    return ResponseEntity.accepted().build();
  }
}
