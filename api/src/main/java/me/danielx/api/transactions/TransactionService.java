package me.danielx.api.transactions;

import me.danielx.api.accounts.Account;
import me.danielx.api.accounts.AccountRepository;
import me.danielx.api.transactions.dto.CreateTransactionRequest;
import me.danielx.api.transactions.dto.CreateTransactionResponse;
import me.danielx.api.users.User;
import me.danielx.api.users.UserRepository;
import me.danielx.api.users.dto.AuthenticatedUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

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
      AuthenticatedUser authenticatedUser, UUID accountId, BigDecimal amount, String currency) {
    User user = userRepository.findById(authenticatedUser.id()).orElseThrow();
    Account account = accountRepository.findByPublicId(accountId).orElseThrow();

    if (!currency.equalsIgnoreCase(account.getCurrency())) {
      throw new IllegalArgumentException("Currency does not match account currency");
    }

    Transaction transaction =
        Transaction.builder()
            .account(account)
            .user(user)
            .sourceType(SourceType.MANUAL)
            .externalId(null)
            .amount(amount)
            .currencyCode(currency)
            .build();

    try {
      Transaction savedTransaction = transactionRepository.saveAndFlush(transaction);
      return CreateTransactionResponse.builder()
          .id(savedTransaction.getPublicId())
          .accountId(accountId)
          .amount(amount)
          .currencyCode(currency)
          .createdAt(savedTransaction.getCreatedAt())
          .build();
    } catch (DataIntegrityViolationException ex) {
      throw new TransactionAlreadyExistsException("Transaction already exists");
    }
  }
}
