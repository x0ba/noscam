package me.danielx.api.transactions;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import me.danielx.api.common.accounts.Account;
import me.danielx.api.common.accounts.AccountNotFoundException;
import me.danielx.api.common.accounts.AccountRepository;
import me.danielx.api.common.idempotency.HttpMethod;
import me.danielx.api.common.idempotency.IdempotencyRecord;
import me.danielx.api.common.idempotency.IdempotencyService;
import me.danielx.api.common.utils.RequestHasher;
import me.danielx.api.notifications.NotificationType;
import me.danielx.api.risk.RiskAssessment;
import me.danielx.api.risk.RiskAssessmentRepository;
import me.danielx.api.risk.RiskLevel;
import me.danielx.api.risk.RiskScoringService;
import me.danielx.api.transactions.dto.CreateTransactionFingerprint;
import me.danielx.api.transactions.dto.CreateTransactionRequest;
import me.danielx.api.transactions.dto.CreateTransactionResponse;
import me.danielx.api.transactions.dto.TransactionDetailResponse;
import me.danielx.api.transactions.dto.TransactionResponse;
import me.danielx.api.users.AuthenticatedUserNotFoundException;
import me.danielx.api.users.User;
import me.danielx.api.users.UserRepository;
import me.danielx.api.users.dto.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TransactionService {
  private static final String ENDPOINT = "/api/v1/transactions";

  private final AccountRepository accountRepository;
  private final UserRepository userRepository;
  private final TransactionRepository transactionRepository;
  private final TransactionIngestionService transactionIngestionService;
  private final RiskAssessmentRepository riskAssessmentRepository;
  private final RiskScoringService riskScoringService;
  private final RequestHasher requestHasher;
  private final IdempotencyService idempotencyService;
  private final ObjectMapper objectMapper;

  public TransactionService(
      AccountRepository accountRepository,
      UserRepository userRepository,
      TransactionRepository transactionRepository,
      TransactionIngestionService transactionIngestionService,
      RiskAssessmentRepository riskAssessmentRepository,
      RiskScoringService riskScoringService,
      RequestHasher requestHasher,
      IdempotencyService idempotencyService,
      ObjectMapper objectMapper) {
    this.accountRepository = accountRepository;
    this.userRepository = userRepository;
    this.transactionRepository = transactionRepository;
    this.transactionIngestionService = transactionIngestionService;
    this.riskAssessmentRepository = riskAssessmentRepository;
    this.riskScoringService = riskScoringService;
    this.requestHasher = requestHasher;
    this.idempotencyService = idempotencyService;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public CreateTransactionResponse manuallyCreateTransaction(
      AuthenticatedUser authenticatedUser,
      CreateTransactionRequest request,
      String idempotencyKey) {
    User user =
        userRepository
            .findById(authenticatedUser.id())
            .orElseThrow(AuthenticatedUserNotFoundException::new);

    CreateTransactionFingerprint fingerprint =
        new CreateTransactionFingerprint(
            request.accountId(),
            request.amount().stripTrailingZeros().toPlainString(),
            request.currency().toUpperCase(Locale.ROOT),
            request.merchant(),
            request.displayName(),
            request.originalDescription(),
            Objects.toString(request.authorizedAt(), null),
            Objects.toString(request.postedAt(), null),
            request.pending(),
            request.category(),
            request.paymentChannel(),
            request.merchantCountry());
    String requestHash = requestHasher.hash(fingerprint);

    IdempotencyRecord record =
        idempotencyService
            .begin(user, HttpMethod.POST, ENDPOINT, idempotencyKey, requestHash)
            .orElseThrow();
    if (idempotencyService.isReplayable(record)) {
      return objectMapper.convertValue(record.getResponseBody(), CreateTransactionResponse.class);
    }
    record = idempotencyService.requireFreshLease(record);

    try {
      Account account =
          accountRepository
              .findByPublicIdAndUserId(request.accountId(), authenticatedUser.id())
              .orElseThrow(() -> new AccountNotFoundException(request.accountId()));
      if (!request.currency().equalsIgnoreCase(account.getCurrency())) {
        throw new TransactionCurrencyMismatchException(request.currency(), account.getCurrency());
      }

      Transaction saved =
          transactionIngestionService.ingest(
              new IngestedTransaction(
                  account,
                  user,
                  SourceType.MANUAL,
                  null,
                  request.amount(),
                  account.getCurrency(),
                  request.merchant(),
                  request.displayName(),
                  request.originalDescription(),
                  request.authorizedAt(),
                  request.postedAt(),
                  Boolean.TRUE.equals(request.pending()),
                  null,
                  request.category(),
                  request.paymentChannel(),
                  request.merchantCountry(),
                  TransactionStatus.ACTIVE),
              NotificationType.RISK_ALERT);
      RiskAssessment assessment =
          riskAssessmentRepository
              .findFirstByTransactionIdOrderByScoredAtDesc(saved.getId())
              .orElse(null);
      CreateTransactionResponse response = CreateTransactionResponse.from(saved, assessment);
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
  public Page<TransactionResponse> list(
      AuthenticatedUser currentUser,
      UUID accountId,
      RiskLevel riskLevel,
      Integer minScore,
      Instant from,
      Instant to,
      Boolean pending,
      Pageable pageable) {
    Specification<Transaction> spec =
        (root, query, cb) -> {
          if (query.getResultType() != Long.class && query.getResultType() != long.class) {
            root.fetch("account", JoinType.INNER);
          }
          List<Predicate> predicates = new ArrayList<>();
          predicates.add(cb.equal(root.get("user").get("id"), currentUser.id()));
          predicates.add(cb.equal(root.get("status"), TransactionStatus.ACTIVE));
          if (accountId != null) {
            predicates.add(cb.equal(root.get("account").get("publicId"), accountId));
          }
          if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("postedAt"), from));
          }
          if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("postedAt"), to));
          }
          if (pending != null) {
            predicates.add(cb.equal(root.get("pending"), pending));
          }
          return cb.and(predicates.toArray(Predicate[]::new));
        };

    Page<Transaction> page = transactionRepository.findAll(spec, pageable);
    Map<Long, RiskAssessment> assessments = latestAssessments(page.getContent());
    Page<TransactionResponse> mapped =
        page.map(txn -> TransactionResponse.from(txn, assessments.get(txn.getId())));
    if (riskLevel == null && minScore == null) {
      return mapped;
    }
    List<TransactionResponse> filtered =
        mapped.getContent().stream()
            .filter(
                item ->
                    (riskLevel == null
                            || (item.riskLevel() != null
                                && item.riskLevel().equals(TransactionResponse.toApiLevel(riskLevel))))
                        && (minScore == null
                            || (item.riskScore() != null && item.riskScore() >= minScore)))
            .toList();
    return new org.springframework.data.domain.PageImpl<>(
        filtered, pageable, mapped.getTotalElements());
  }

  @Transactional(readOnly = true)
  public TransactionDetailResponse get(AuthenticatedUser currentUser, UUID transactionId) {
    Transaction transaction =
        transactionRepository
            .findByPublicIdAndUserId(transactionId, currentUser.id())
            .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    RiskAssessment assessment =
        riskAssessmentRepository
            .findFirstByTransactionIdOrderByScoredAtDesc(transaction.getId())
            .orElse(null);
    return TransactionDetailResponse.from(transaction, assessment);
  }

  @Transactional
  public TransactionDetailResponse rescore(AuthenticatedUser currentUser, UUID transactionId) {
    Transaction transaction =
        transactionRepository
            .findByPublicIdAndUserId(transactionId, currentUser.id())
            .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    transaction.setContentHash(null);
    RiskAssessment assessment = riskScoringService.score(transaction, NotificationType.RISK_ALERT);
    return TransactionDetailResponse.from(transaction, assessment);
  }

  private Map<Long, RiskAssessment> latestAssessments(List<Transaction> transactions) {
    List<Long> ids = transactions.stream().map(Transaction::getId).toList();
    return riskAssessmentRepository.findByTransactionIdIn(ids).stream()
        .collect(
            Collectors.toMap(
                assessment -> assessment.getTransaction().getId(),
                Function.identity(),
                (left, right) -> left.getScoredAt().isAfter(right.getScoredAt()) ? left : right));
  }
}
