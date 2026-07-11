package me.danielx.api.accounts;

import me.danielx.api.accounts.dto.AccountListResponse;
import me.danielx.api.accounts.dto.AccountResponse;
import me.danielx.api.accounts.dto.CreateAccountRequest;
import me.danielx.api.users.User;
import me.danielx.api.users.UserRepository;
import me.danielx.api.users.dto.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
  private final AccountRepository accountRepository;
  private final UserRepository userRepository;

  public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
    this.accountRepository = accountRepository;
    this.userRepository = userRepository;
  }

  public Page<AccountListResponse> getAllAccountsForCurrentUser(
      AuthenticatedUser currentUser, Pageable pageable) {
    Long userId = currentUser.id();
    return accountRepository
        .findAllByUserId(userId, pageable)
        .map(AccountService::toAccountListResponse);
  }

  private static AccountListResponse toAccountListResponse(Account account) {
    return new AccountListResponse(
        account.getPublicId(), account.getBank(), account.getAccountName(), account.getBalance());
  }

  public AccountResponse createAccountForCurrentUser(
      AuthenticatedUser currentUser, CreateAccountRequest request) {
    User currentUserEntity = userRepository.findById(currentUser.id()).orElseThrow();
    Account account =
        Account.builder()
            .user(currentUserEntity)
            .bank(request.bank())
            .accountName(request.accountName())
            .accountType(request.type())
            .currency(request.currency())
            .build();
    Account savedAccount = accountRepository.save(account);
    return AccountResponse.fromAccount(savedAccount);
  }
}
