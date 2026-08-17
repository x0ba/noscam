package me.danielx.api.common.jobs;

import me.danielx.api.plaid.PlaidItem;
import me.danielx.api.users.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
public class SyncJobService {
  private static final int MAX_ATTEMPTS = 8;

  private final SyncJobRepository syncJobRepository;

  public SyncJobService(SyncJobRepository syncJobRepository) {
    this.syncJobRepository = syncJobRepository;
  }

  @Transactional
  public SyncJob enqueue(User user, PlaidItem item, JobType type, Map<String, Object> payload) {
    SyncJob job =
        SyncJob.builder()
            .user(user)
            .plaidItem(item)
            .jobType(type)
            .state(JobState.PENDING)
            .attemptCount(0)
            .maxAttempts(MAX_ATTEMPTS)
            .nextAttemptAt(Instant.now())
            .payload(payload == null ? Map.of() : payload)
            .build();
    return syncJobRepository.save(job);
  }

  @Transactional
  public SyncJob claimNext() {
    return syncJobRepository.findClaimable(Instant.now()).stream()
        .findFirst()
        .map(
            job -> {
              job.setState(JobState.RUNNING);
              job.setAttemptCount(job.getAttemptCount() + 1);
              return syncJobRepository.save(job);
            })
        .orElse(null);
  }

  @Transactional
  public void markSucceeded(SyncJob job) {
    job.setState(JobState.SUCCEEDED);
    job.setLastError(null);
    syncJobRepository.save(job);
  }

  @Transactional
  public void markFailed(SyncJob job, String error) {
    job.setLastError(error);
    if (job.getAttemptCount() >= job.getMaxAttempts()) {
      job.setState(JobState.DEAD);
    } else {
      job.setState(JobState.FAILED);
      long backoffSeconds = Math.min(3600, (long) Math.pow(2, job.getAttemptCount()));
      job.setNextAttemptAt(Instant.now().plus(Duration.ofSeconds(backoffSeconds)));
    }
    syncJobRepository.save(job);
  }
}
