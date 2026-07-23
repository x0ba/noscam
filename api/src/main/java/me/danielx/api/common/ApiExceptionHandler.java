package me.danielx.api.common;

import me.danielx.api.accounts.AccountNotFoundException;
import me.danielx.api.accounts.AccountCreationException;
import me.danielx.api.auth.EmailAlreadyExistsException;
import me.danielx.api.auth.UserRegistrationException;
import me.danielx.api.transactions.TransactionAlreadyExistsException;
import me.danielx.api.transactions.TransactionCreationException;
import me.danielx.api.transactions.TransactionCurrencyMismatchException;
import me.danielx.api.users.AuthenticatedUserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ProblemDetail> handleEmailAlreadyExistsException(
      EmailAlreadyExistsException ex) {
    return problem(HttpStatus.CONFLICT, ex);
  }

  @ExceptionHandler(TransactionAlreadyExistsException.class)
  public ResponseEntity<ProblemDetail> handleTransactionAlreadyExistsException(
      TransactionAlreadyExistsException ex) {
    return problem(HttpStatus.CONFLICT, ex);
  }

  @ExceptionHandler(AccountNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleAccountNotFoundException(
      AccountNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, ex);
  }

  @ExceptionHandler(TransactionCurrencyMismatchException.class)
  public ResponseEntity<ProblemDetail> handleTransactionCurrencyMismatchException(
      TransactionCurrencyMismatchException ex) {
    return problem(HttpStatus.UNPROCESSABLE_ENTITY, ex);
  }

  @ExceptionHandler(AuthenticatedUserNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleAuthenticatedUserNotFoundException(
      AuthenticatedUserNotFoundException ex) {
    return problem(HttpStatus.UNAUTHORIZED, ex);
  }

  @ExceptionHandler({
    AccountCreationException.class,
    TransactionCreationException.class,
    UserRegistrationException.class
  })
  public ResponseEntity<ProblemDetail> handlePersistenceException(RuntimeException ex) {
    return problem(HttpStatus.INTERNAL_SERVER_ERROR, ex);
  }

  private ResponseEntity<ProblemDetail> problem(HttpStatus status, RuntimeException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
    return ResponseEntity.status(status).body(problem);
  }
}
