package me.danielx.api.plaid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/plaid")
@Tag(name = "Plaid", description = "Bank connections through Plaid Link")
public class PlaidWebhookController {
  private final PlaidWebhookService plaidWebhookService;

  public PlaidWebhookController(PlaidWebhookService plaidWebhookService) {
    this.plaidWebhookService = plaidWebhookService;
  }

  @PostMapping("/webhook")
  @Operation(summary = "Receive a Plaid webhook")
  public ResponseEntity<Void> webhook(
      @RequestHeader(value = "Plaid-Verification", required = false) String verification,
      @RequestBody String rawBody) {
    plaidWebhookService.capture(verification, rawBody);
    return ResponseEntity.ok().build();
  }
}
