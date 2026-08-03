package me.danielx.api.common.idempotency;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<IdempotencyRecord> findByUserIdAndHttpMethodAndEndpointAndIdempotencyKey(
      Long userId, HttpMethod httpMethod, String endpoint, String idempotencyKey);
}
