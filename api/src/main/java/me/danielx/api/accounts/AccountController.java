package me.danielx.api.accounts;

import me.danielx.api.accounts.dto.AccountListResponse;
import me.danielx.api.accounts.dto.AccountResponse;
import me.danielx.api.accounts.dto.CreateAccountRequest;
import me.danielx.api.users.dto.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

  AccountService accountService;

  @GetMapping("/")
  public Page<AccountListResponse> getAccountsForCurrentUser(
      @AuthenticationPrincipal AuthenticatedUser currentUser,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return accountService.getAllAccountsForCurrentUser(currentUser, pageable);
  }

  @PostMapping("/")
  public ResponseEntity<AccountResponse> createAccountForCurrentUser(
      @AuthenticationPrincipal AuthenticatedUser currentUser,
      @RequestBody CreateAccountRequest request) {
    AccountResponse accountResponse =
        accountService.createAccountForCurrentUser(currentUser, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);
  }
}
