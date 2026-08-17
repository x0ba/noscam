package me.danielx.api.common.jobs;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SyncJobRepository extends JpaRepository<SyncJob, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      """
      select j from SyncJob j
      where j.state in (me.danielx.api.common.jobs.JobState.PENDING, me.danielx.api.common.jobs.JobState.FAILED)
        and j.nextAttemptAt <= :now
      order by j.nextAttemptAt asc
      """)
  List<SyncJob> findClaimable(@Param("now") Instant now);

  Optional<SyncJob> findByIdAndUserId(Long id, Long userId);
}
