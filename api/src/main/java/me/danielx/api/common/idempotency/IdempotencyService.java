package me.danielx.api.common.idempotency;

import me.danielx.api.users.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class IdempotencyService {
  public static final int LOCK_EXPIRES_IN_SECONDS = 20;
  public static final int EXPIRES_IN_SECONDS = 24 * 60 * 60;

  private final IdempotencyRepository idempotencyRepository;

  public IdempotencyService(IdempotencyRepository idempotencyRepository) {
    this.idempotencyRepository = idempotencyRepository;
  }

  @Transactional
  public Optional<IdempotencyRecord> begin(
      User user, HttpMethod httpMethod, String endpoint, String idempotencyKey, String requestHash) {
    Instant now = Instant.now();
    Optional<IdempotencyRecord> existing =
        idempotencyRepository.findByUserIdAndHttpMethodAndEndpointAndIdempotencyKey(
            user.getId(), httpMethod, endpoint, idempotencyKey);

    if (existing.isPresent()) {
      return Optional.of(reuseOrReject(existing.get(), requestHash, now));
    }

    IdempotencyRecord created =
        IdempotencyRecord.builder()
            .idempotencyKey(idempotencyKey)
            .user(user)
            .endpoint(endpoint)
            .requestHash(requestHash)
            .httpMethod(httpMethod)
            .status(RequestStatus.PROCESSING)
            .lockExpiresAt(now.plusSeconds(LOCK_EXPIRES_IN_SECONDS))
            .expiresAt(now.plusSeconds(EXPIRES_IN_SECONDS))
            .build();

    try {
      return Optional.of(idempotencyRepository.saveAndFlush(created));
    } catch (DataIntegrityViolationException ex) {
      IdempotencyRecord raced =
          idempotencyRepository
              .findByUserIdAndHttpMethodAndEndpointAndIdempotencyKey(
                  user.getId(), httpMethod, endpoint, idempotencyKey)
              .orElseThrow(() -> ex);
      return Optional.of(reuseOrReject(raced, requestHash, Instant.now()));
    }
  }

  public IdempotencyRecord requireFreshLease(IdempotencyRecord record) {
    if (record.getStatus() == RequestStatus.COMPLETED) {
      return record;
    }
    if (record.getStatus() == RequestStatus.PROCESSING
        && Instant.now().isBefore(record.getLockExpiresAt())) {
      throw new IdempotencyRequestInProgressException();
    }
    record.setStatus(RequestStatus.PROCESSING);
    record.setLockExpiresAt(Instant.now().plusSeconds(LOCK_EXPIRES_IN_SECONDS));
    record.setResponseCode(null);
    record.setResponseBody(null);
    record.setResponseHeaders(null);
    return idempotencyRepository.save(record);
  }

  @Transactional
  public void complete(
      IdempotencyRecord record,
      int responseCode,
      Map<String, Object> responseBody,
      Map<String, List<String>> responseHeaders) {
    record.setStatus(RequestStatus.COMPLETED);
    record.setResponseCode(responseCode);
    record.setResponseBody(responseBody);
    record.setResponseHeaders(responseHeaders);
    record.setLockExpiresAt(Instant.now());
    idempotencyRepository.save(record);
  }

  @Transactional
  public void fail(IdempotencyRecord record) {
    record.setStatus(RequestStatus.FAILED);
    record.setResponseCode(null);
    record.setResponseBody(null);
    record.setResponseHeaders(null);
    record.setLockExpiresAt(Instant.now());
    idempotencyRepository.save(record);
  }

  public boolean isReplayable(IdempotencyRecord record) {
    return record.getStatus() == RequestStatus.COMPLETED && record.getResponseBody() != null;
  }

  private IdempotencyRecord reuseOrReject(
      IdempotencyRecord record, String requestHash, Instant now) {
    if (!record.getRequestHash().equals(requestHash)) {
      throw new IdempotencyRequestHashMismatchException();
    }
    if (record.getExpiresAt().isBefore(now)) {
      record.setStatus(RequestStatus.PROCESSING);
      record.setLockExpiresAt(now.plusSeconds(LOCK_EXPIRES_IN_SECONDS));
      record.setExpiresAt(now.plusSeconds(EXPIRES_IN_SECONDS));
      record.setResponseCode(null);
      record.setResponseBody(null);
      record.setResponseHeaders(null);
      return idempotencyRepository.save(record);
    }
    if (record.getStatus() == RequestStatus.COMPLETED) {
      return record;
    }
    if (record.getStatus() == RequestStatus.PROCESSING
        && now.isBefore(record.getLockExpiresAt())) {
      throw new IdempotencyRequestInProgressException();
    }
    record.setStatus(RequestStatus.PROCESSING);
    record.setLockExpiresAt(now.plusSeconds(LOCK_EXPIRES_IN_SECONDS));
    return idempotencyRepository.save(record);
  }
}
