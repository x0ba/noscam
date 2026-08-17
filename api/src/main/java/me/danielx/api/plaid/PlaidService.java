package me.danielx.api.plaid;

import me.danielx.api.common.accounts.Account;
import me.danielx.api.common.accounts.AccountRepository;
import me.danielx.api.common.accounts.AccountSourceType;
import me.danielx.api.common.accounts.AccountStatus;
import me.danielx.api.common.accounts.AccountType;
import me.danielx.api.common.crypto.AesGcmEncryptor;
import me.danielx.api.common.idempotency.HttpMethod;
import me.danielx.api.common.idempotency.IdempotencyRecord;
import me.danielx.api.common.idempotency.IdempotencyService;
import me.danielx.api.common.jobs.JobType;
import me.danielx.api.common.jobs.SyncJobService;
import me.danielx.api.common.ratelimit.InMemoryRateLimiter;
import me.danielx.api.common.utils.RequestHasher;
import me.danielx.api.notifications.NotificationType;
import me.danielx.api.plaid.dto.ExchangePublicTokenRequest;
import me.danielx.api.plaid.dto.LinkTokenResponse;
import me.danielx.api.plaid.dto.PlaidItemResponse;
import me.danielx.api.transactions.IngestedTransaction;
import me.danielx.api.transactions.SourceType;
import me.danielx.api.transactions.TransactionIngestionService;
import me.danielx.api.transactions.TransactionStatus;
import me.danielx.api.users.AuthenticatedUserNotFoundException;
import me.danielx.api.users.User;
import me.danielx.api.users.UserRepository;
import me.danielx.api.users.dto.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class PlaidService {
  private static final Logger log = LoggerFactory.getLogger(PlaidService.class);
  private static final String EXCHANGE_ENDPOINT = "/api/v1/plaid/exchange";

  private final PlaidClient plaidClient;
  private final PlaidProperties properties;
  private final PlaidItemRepository plaidItemRepository;
  private final AccountRepository accountRepository;
  private final UserRepository userRepository;
  private final AesGcmEncryptor encryptor;
  private final TransactionIngestionService transactionIngestionService;
  private final SyncJobService syncJobService;
  private final IdempotencyService idempotencyService;
  private final RequestHasher requestHasher;
  private final ObjectMapper objectMapper;
  private final InMemoryRateLimiter rateLimiter;

  public PlaidService(
      PlaidClient plaidClient,
      PlaidProperties properties,
      PlaidItemRepository plaidItemRepository,
      AccountRepository accountRepository,
      UserRepository userRepository,
      AesGcmEncryptor encryptor,
      TransactionIngestionService transactionIngestionService,
      SyncJobService syncJobService,
      IdempotencyService idempotencyService,
      RequestHasher requestHasher,
      ObjectMapper objectMapper,
      InMemoryRateLimiter rateLimiter) {
    this.plaidClient = plaidClient;
    this.properties = properties;
    this.plaidItemRepository = plaidItemRepository;
    this.accountRepository = accountRepository;
    this.userRepository = userRepository;
    this.encryptor = encryptor;
    this.transactionIngestionService = transactionIngestionService;
    this.syncJobService = syncJobService;
    this.idempotencyService = idempotencyService;
    this.requestHasher = requestHasher;
    this.objectMapper = objectMapper;
    this.rateLimiter = rateLimiter;
  }

  @Transactional
  public LinkTokenResponse createLinkToken(AuthenticatedUser currentUser) {
    User user = requireUser(currentUser);
    PlaidModels.LinkTokenResult result =
        plaidClient.createLinkToken(user.getPublicId().toString(), properties.webhookUrl());
    return new LinkTokenResponse(result.linkToken(), result.expiration());
  }

  @Transactional
  public PlaidItemResponse exchange(
      AuthenticatedUser currentUser, ExchangePublicTokenRequest request, String idempotencyKey) {
    User user = requireUser(currentUser);
    String requestHash = requestHasher.hash(request);
    IdempotencyRecord record =
        idempotencyService
            .begin(user, HttpMethod.POST, EXCHANGE_ENDPOINT, idempotencyKey, requestHash)
            .orElseThrow();
    if (idempotencyService.isReplayable(record)) {
      return objectMapper.convertValue(record.getResponseBody(), PlaidItemResponse.class);
    }
    record = idempotencyService.requireFreshLease(record);
    try {
      PlaidModels.ExchangeResult exchanged = plaidClient.exchangePublicToken(request.publicToken());
      PlaidItem item =
          plaidItemRepository
              .findByPlaidItemId(exchanged.itemId())
              .orElseGet(() -> PlaidItem.builder().user(user).plaidItemId(exchanged.itemId()).build());
      if (!item.getUser().getId().equals(user.getId())) {
        throw new InvalidPlaidWebhookException("Plaid item already belongs to another user");
      }
      item.setAccessToken(encryptor.encrypt(exchanged.accessToken()));
      item.setInstitutionId(exchanged.institutionId());
      item.setInstitutionName(
          exchanged.institutionName() == null ? "Linked bank" : exchanged.institutionName());
      item.setStatus(PlaidItemStatus.ACTIVE);
      item.setLastErrorCode(null);
      PlaidItem saved = plaidItemRepository.saveAndFlush(item);
      upsertAccounts(saved, encryptor.decrypt(saved.getAccessToken()));
      syncJobService.enqueue(user, saved, JobType.PLAID_INITIAL_SYNC, Map.of());
      PlaidItemResponse response = PlaidItemResponse.from(saved);
      idempotencyService.complete(
          record,
          HttpStatus.CREATED.value(),
          objectMapper.convertValue(response, new TypeReference<Map<String, Object>>() {}),
          Map.of());
      return response;
    } catch (RuntimeException ex) {
      idempotencyService.fail(record);
      throw ex;
    }
  }

  @Transactional(readOnly = true)
  public List<PlaidItemResponse> listItems(AuthenticatedUser currentUser) {
    return plaidItemRepository.findAllByUserIdOrderByConnectedAtDesc(currentUser.id()).stream()
        .map(PlaidItemResponse::from)
        .toList();
  }

  @Transactional
  public void disconnect(AuthenticatedUser currentUser, UUID itemId) {
    PlaidItem item = requireItem(currentUser, itemId);
    try {
      plaidClient.removeItem(encryptor.decrypt(item.getAccessToken()));
    } catch (Exception ex) {
      log.warn("Could not revoke Plaid item {}", item.getPlaidItemId());
    }
    item.setStatus(PlaidItemStatus.DISCONNECTED);
    item.setLastErrorCode(null);
    accountRepository
        .findAllByPlaidItem(item)
        .forEach(account -> account.setStatus(AccountStatus.DISCONNECTED));
  }

  @Transactional
  public void requestSync(AuthenticatedUser currentUser, UUID itemId) {
    PlaidItem item = requireItem(currentUser, itemId);
    if (!rateLimiter.tryAcquire(
        "plaid-sync:" + currentUser.id() + ":" + item.getId(), 5, Duration.ofMinutes(15))) {
      throw new PlaidRateLimitedException(60);
    }
    syncJobService.enqueue(item.getUser(), item, JobType.PLAID_TRANSACTIONS_SYNC, Map.of("manual", true));
  }

  @Transactional
  public void syncItem(PlaidItem detached) {
    PlaidItem item = plaidItemRepository.findById(detached.getId()).orElseThrow();
    if (item.getStatus() != PlaidItemStatus.ACTIVE) {
      return;
    }
    String accessToken = encryptor.decrypt(item.getAccessToken());
    upsertAccounts(item, accessToken);
    String cursor = item.getCursor();
    boolean hasMore = true;
    String workingCursor = cursor;
    while (hasMore) {
      PlaidModels.SyncPage page = plaidClient.syncTransactions(accessToken, workingCursor);
      applyPage(item, page);
      workingCursor = page.nextCursor();
      hasMore = page.hasMore();
    }
    item.setCursor(workingCursor);
    item.setLastSuccessfulSync(Instant.now());
    item.setLastErrorCode(null);
    plaidItemRepository.save(item);
  }

  private void applyPage(PlaidItem item, PlaidModels.SyncPage page) {
    Map<String, Account> accountsByProvider =
        accountRepository.findAllByPlaidItem(item).stream()
            .filter(account -> account.getProviderAccountId() != null)
            .collect(java.util.stream.Collectors.toMap(Account::getProviderAccountId, account -> account));
    page.added().forEach(txn -> ingest(item, accountsByProvider, txn, TransactionStatus.ACTIVE));
    page.modified().forEach(txn -> ingest(item, accountsByProvider, txn, TransactionStatus.ACTIVE));
    page.removed()
        .forEach(txn -> ingest(item, accountsByProvider, txn, TransactionStatus.REMOVED));
  }

  private void ingest(
      PlaidItem item,
      Map<String, Account> accountsByProvider,
      PlaidModels.PlaidTransaction txn,
      TransactionStatus status) {
    Account account = accountsByProvider.get(txn.accountId());
    if (account == null) {
      log.warn("Skipping Plaid transaction for unknown account {}", txn.accountId());
      return;
    }
    Instant posted =
        txn.date() == null ? Instant.now() : txn.date().atStartOfDay().toInstant(ZoneOffset.UTC);
    Instant authorized =
        txn.authorizedDate() == null
            ? null
            : txn.authorizedDate().atStartOfDay().toInstant(ZoneOffset.UTC);
    transactionIngestionService.ingest(
        new IngestedTransaction(
            account,
            item.getUser(),
            SourceType.PLAID,
            txn.transactionId(),
            txn.amount() == null ? BigDecimal.ZERO : txn.amount().negate(),
            txn.isoCurrencyCode() == null ? account.getCurrency() : txn.isoCurrencyCode(),
            txn.merchantName(),
            txn.name(),
            txn.originalDescription(),
            authorized,
            posted,
            txn.pending(),
            txn.pendingTransactionId(),
            txn.category() == null || txn.category().isEmpty() ? null : String.join(", ", txn.category()),
            txn.paymentChannel(),
            txn.merchantCountry(),
            status),
        NotificationType.RISK_ALERT);
  }

  private void upsertAccounts(PlaidItem item, String accessToken) {
    for (PlaidModels.PlaidAccount plaidAccount : plaidClient.getAccounts(accessToken)) {
      Account account =
          accountRepository
              .findByPlaidItemAndProviderAccountId(item, plaidAccount.accountId())
              .orElseGet(
                  () ->
                      Account.builder()
                          .user(item.getUser())
                          .plaidItem(item)
                          .sourceType(AccountSourceType.PLAID)
                          .providerAccountId(plaidAccount.accountId())
                          .build());
      account.setBank(
          item.getInstitutionName() == null ? "Linked bank" : item.getInstitutionName());
      account.setAccountName(
          firstNonBlank(plaidAccount.officialName(), plaidAccount.name(), "Account"));
      account.setAccountType(mapType(plaidAccount.type(), plaidAccount.subtype()));
      account.setBalance(
          plaidAccount.currentBalance() == null ? BigDecimal.ZERO : plaidAccount.currentBalance());
      account.setCurrency(
          plaidAccount.isoCurrencyCode() == null ? "USD" : plaidAccount.isoCurrencyCode());
      account.setMask(plaidAccount.mask());
      account.setStatus(AccountStatus.ACTIVE);
      accountRepository.save(account);
    }
  }

  private AccountType mapType(String type, String subtype) {
    String haystack = ((type == null ? "" : type) + " " + (subtype == null ? "" : subtype)).toLowerCase(Locale.ROOT);
    if (haystack.contains("credit")) {
      return AccountType.CREDIT_CARD;
    }
    if (haystack.contains("saving")) {
      return AccountType.SAVINGS;
    }
    if (haystack.contains("brokerage") || haystack.contains("investment")) {
      return AccountType.INVESTMENT;
    }
    return AccountType.CHECKING;
  }

  private PlaidItem requireItem(AuthenticatedUser currentUser, UUID itemId) {
    return plaidItemRepository
        .findByPublicIdAndUserId(itemId, currentUser.id())
        .orElseThrow(() -> new PlaidItemNotFoundException(itemId));
  }

  private User requireUser(AuthenticatedUser currentUser) {
    return userRepository
        .findById(currentUser.id())
        .orElseThrow(AuthenticatedUserNotFoundException::new);
  }

  private static String firstNonBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return "Account";
  }
}
