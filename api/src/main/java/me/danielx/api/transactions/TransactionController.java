package me.danielx.api.transactions;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.danielx.api.risk.RiskLevel;
import me.danielx.api.transactions.dto.CreateTransactionRequest;
import me.danielx.api.transactions.dto.CreateTransactionResponse;
import me.danielx.api.transactions.dto.TransactionDetailResponse;
import me.danielx.api.transactions.dto.TransactionResponse;
import me.danielx.api.users.dto.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Transactions owned by the authenticated user")
@SecurityRequirement(name = "sessionCookie")
public class TransactionController {

  private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
  private static final String BASE_URI = "/api/v1/transactions";

  private final TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @GetMapping
  @Operation(summary = "List transactions for the current user")
  public ResponseEntity<Page<TransactionResponse>> list(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @RequestParam(required = false) UUID accountId,
      @RequestParam(required = false) RiskLevel riskLevel,
      @RequestParam(required = false) Integer minScore,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
      @RequestParam(required = false) Boolean pending,
      @PageableDefault(sort = "postedAt", direction = Sort.Direction.DESC) Pageable pageable) {
    return ResponseEntity.ok(
        transactionService.list(
            authenticatedUser, accountId, riskLevel, minScore, from, to, pending, pageable));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a transaction and its current risk assessment")
  public ResponseEntity<TransactionDetailResponse> get(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser, @PathVariable UUID id) {
    return ResponseEntity.ok(transactionService.get(authenticatedUser, id));
  }

  @PostMapping("/{id}/rescore")
  @Operation(summary = "Rescore a single transaction")
  public ResponseEntity<TransactionDetailResponse> rescore(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser, @PathVariable UUID id) {
    return ResponseEntity.ok(transactionService.rescore(authenticatedUser, id));
  }

  @PostMapping
  @Operation(summary = "Manually create a transaction")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Transaction created"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "404", description = "Account not found"),
    @ApiResponse(responseCode = "409", description = "Transaction already exists"),
    @ApiResponse(responseCode = "422", description = "Currency does not match the account")
  })
  public ResponseEntity<CreateTransactionResponse> manuallyCreateTransaction(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      @RequestHeader(IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @Valid @RequestBody CreateTransactionRequest request) {

    CreateTransactionResponse transaction =
        transactionService.manuallyCreateTransaction(authenticatedUser, request, idempotencyKey);

    URI location = URI.create(BASE_URI + "/" + transaction.id());
    return ResponseEntity.created(location).body(transaction);
  }
}
