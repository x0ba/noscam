package me.danielx.api.accounts;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.danielx.api.accounts.dto.AccountListResponse;
import me.danielx.api.accounts.dto.AccountResponse;
import me.danielx.api.accounts.dto.CreateAccountRequest;
import me.danielx.api.users.dto.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts", description = "Accounts owned by the authenticated user")
@SecurityRequirement(name = "sessionCookie")
public class AccountController {

  private final AccountService accountService;

  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  @GetMapping
  @Operation(summary = "List the current user's accounts")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Paginated accounts returned"),
    @ApiResponse(responseCode = "401", description = "Authentication is required")
  })
  public ResponseEntity<Page<AccountListResponse>> getAccountsForCurrentUser(
      @AuthenticationPrincipal AuthenticatedUser currentUser,
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<AccountListResponse> page =
        accountService.getAllAccountsForCurrentUser(currentUser, pageable);
    return ResponseEntity.ok(page);
  }

  @PostMapping
  @Operation(summary = "Create an account for the current user")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Account created"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "401", description = "Authentication is required")
  })
  public ResponseEntity<AccountResponse> createAccountForCurrentUser(
      @AuthenticationPrincipal AuthenticatedUser currentUser,
      @Valid @RequestBody CreateAccountRequest request) {

    AccountResponse accountResponse =
        accountService.createAccountForCurrentUser(currentUser, request);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(accountResponse.id())
            .toUri();

    return ResponseEntity.created(location).body(accountResponse);
  }
}
