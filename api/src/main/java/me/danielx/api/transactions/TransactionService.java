package me.danielx.api.transactions;

import me.danielx.api.accounts.Account;
import me.danielx.api.accounts.AccountNotFoundException;
import me.danielx.api.accounts.AccountRepository;
import me.danielx.api.transactions.dto.CreateTransactionResponse;
import me.danielx.api.users.AuthenticatedUserNotFoundException;
import me.danielx.api.users.User;
import me.danielx.api.users.UserRepository;
import me.danielx.api.users.dto.AuthenticatedUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static me.danielx.api.global.utils.Sha256Util.sha256;

@Service
public class TransactionService {
  private final AccountRepository accountRepository;
  private final UserRepository userRepository;
  private final TransactionRepository transactionRepository;

  public TransactionService(
      AccountRepository accountRepository,
      UserRepository userRepository,
      TransactionRepository transactionRepository) {
    this.accountRepository = accountRepository;
    this.userRepository = userRepository;
    this.transactionRepository = transactionRepository;
  }

  public CreateTransactionResponse manuallyCreateTransaction(
      AuthenticatedUser authenticatedUser,
      UUID accountId,
      BigDecimal amount,
      String currency,
      String idempotencyKey) {

    StringBuilder toHash = new StringBuilder();
    toHash.append("POST");
    toHash.append("/api/v1/transactions");
    toHash.append(authenticatedUser.id());
    String hash = sha256(toHash.toString());

    User user =
        userRepository
            .findById(authenticatedUser.id())
            .orElseThrow(AuthenticatedUserNotFoundException::new);
    Account account =
        accountRepository
            .findByPublicIdAndUserId(accountId, authenticatedUser.id())
            .orElseThrow(() -> new AccountNotFoundException(accountId));

    if (!currency.equalsIgnoreCase(account.getCurrency())) {
      throw new TransactionCurrencyMismatchException(currency, account.getCurrency());
    }

    Transaction transaction =
        Transaction.builder()
            .account(account)
            .user(user)
            .sourceType(SourceType.MANUAL)
            .externalId(null)
            .amount(amount)
            .currencyCode(account.getCurrency())
            .build();

    Transaction savedTransaction;
    try {
      savedTransaction = transactionRepository.saveAndFlush(transaction);
    } catch (DataIntegrityViolationException ex) {
      throw new TransactionCreationException(ex);
    }
    return CreateTransactionResponse.builder()
        .id(savedTransaction.getPublicId())
        .accountId(accountId)
        .amount(amount)
        .currencyCode(account.getCurrency())
        .createdAt(savedTransaction.getCreatedAt())
        .build();
  }
}
