package me.danielx.api.common;

import me.danielx.api.auth.EmailAlreadyExistsException;
import me.danielx.api.auth.UserRegistrationException;
import me.danielx.api.common.accounts.AccountCreationException;
import me.danielx.api.common.accounts.AccountNotFoundException;
import me.danielx.api.common.idempotency.IdempotencyRequestHashMismatchException;
import me.danielx.api.common.idempotency.IdempotencyRequestInProgressException;
import me.danielx.api.common.jobs.JobNotClaimableException;
import me.danielx.api.notifications.NotificationNotFoundException;
import me.danielx.api.plaid.InvalidPlaidWebhookException;
import me.danielx.api.plaid.PlaidItemNotFoundException;
import me.danielx.api.plaid.PlaidRateLimitedException;
import me.danielx.api.risk.InvalidRiskSettingsException;
import me.danielx.api.transactions.TransactionAlreadyExistsException;
import me.danielx.api.transactions.TransactionCreationException;
import me.danielx.api.transactions.TransactionCurrencyMismatchException;
import me.danielx.api.transactions.TransactionNotFoundException;
import me.danielx.api.users.AuthenticatedUserNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  private static final int RETRY_AFTER_SECONDS = 2;

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ProblemDetail> handleEmailAlreadyExistsException(
      EmailAlreadyExistsException ex) {
    return problem(HttpStatus.CONFLICT, ex);
  }

  @ExceptionHandler({
    TransactionAlreadyExistsException.class,
    JobNotClaimableException.class
  })
  public ResponseEntity<ProblemDetail> handleConflict(RuntimeException ex) {
    return problem(HttpStatus.CONFLICT, ex);
  }

  @ExceptionHandler({
    AccountNotFoundException.class,
    TransactionNotFoundException.class,
    PlaidItemNotFoundException.class,
    NotificationNotFoundException.class
  })
  public ResponseEntity<ProblemDetail> handleNotFound(RuntimeException ex) {
    return problem(HttpStatus.NOT_FOUND, ex);
  }

  @ExceptionHandler({
    TransactionCurrencyMismatchException.class,
    IdempotencyRequestHashMismatchException.class,
    InvalidRiskSettingsException.class
  })
  public ResponseEntity<ProblemDetail> handleUnprocessable(RuntimeException ex) {
    return problem(HttpStatus.UNPROCESSABLE_CONTENT, ex);
  }

  @ExceptionHandler({AuthenticatedUserNotFoundException.class, InvalidPlaidWebhookException.class})
  public ResponseEntity<ProblemDetail> handleUnauthorized(RuntimeException ex) {
    return problem(HttpStatus.UNAUTHORIZED, ex);
  }

  @ExceptionHandler(PlaidRateLimitedException.class)
  public ResponseEntity<ProblemDetail> handleRateLimited(PlaidRateLimitedException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.retryAfterSeconds()))
        .body(problem);
  }

  @ExceptionHandler({
    AccountCreationException.class,
    TransactionCreationException.class,
    UserRegistrationException.class
  })
  public ResponseEntity<ProblemDetail> handlePersistenceException(RuntimeException ex) {
    return problem(HttpStatus.INTERNAL_SERVER_ERROR, ex);
  }

  @ExceptionHandler(IdempotencyRequestInProgressException.class)
  public ResponseEntity<ApiErrorResponse> handleIdempotencyRequestInProgressException(
      IdempotencyRequestInProgressException ex) {
    ApiErrorResponse response =
        new ApiErrorResponse(
            "IDEMPOTENCY_REQUEST_IN_PROGRESS", ex.getMessage(), true, RETRY_AFTER_SECONDS);
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .header(HttpHeaders.RETRY_AFTER, String.valueOf(RETRY_AFTER_SECONDS))
        .body(response);
  }

  private ResponseEntity<ProblemDetail> problem(HttpStatus status, RuntimeException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
    return ResponseEntity.status(status).body(problem);
  }
}
