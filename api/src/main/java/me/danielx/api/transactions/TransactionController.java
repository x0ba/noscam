package me.danielx.api.transactions;

import jakarta.validation.Valid;
import me.danielx.api.accounts.Account;
import me.danielx.api.transactions.dto.CreateTransactionRequest;
import me.danielx.api.transactions.dto.CreateTransactionResponse;
import me.danielx.api.users.dto.AuthenticatedUser;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

  TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @PostMapping
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
