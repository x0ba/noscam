package me.danielx.api.transactions;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.danielx.api.transactions.dto.CreateTransactionRequest;
import me.danielx.api.transactions.dto.CreateTransactionResponse;
import me.danielx.api.users.dto.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Transactions owned by the authenticated user")
@SecurityRequirement(name = "sessionCookie")
public class TransactionController {

  TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
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
      @Valid @RequestBody CreateTransactionRequest request) {
    UUID accountId = request.accountId();
    BigDecimal amount = request.amount();
    String currency = request.currency();

    CreateTransactionResponse transaction =
        transactionService.manuallyCreateTransaction(
            authenticatedUser, accountId, amount, currency);

    URI location = URI.create("/api/v1/transactions/" + transaction.id());

    return ResponseEntity.created(location).body(transaction);
  }
}
