package me.danielx.api.common.jobs;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import me.danielx.api.plaid.PlaidItem;
import me.danielx.api.users.User;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "sync_jobs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncJob {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Builder.Default
  @NotNull
  @Column(nullable = false, unique = true, updatable = false)
  private UUID publicId = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "plaid_item_id")
  private PlaidItem plaidItem;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 64)
  private JobType jobType;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private JobState state;

  @NotNull
  @Builder.Default
  @Column(nullable = false)
  private int attemptCount = 0;

  @NotNull
  @Builder.Default
  @Column(nullable = false)
  private int maxAttempts = 8;

  @NotNull
  @Column(nullable = false)
  private Instant nextAttemptAt;

  @Column(columnDefinition = "text")
  private String lastError;

  @Builder.Default
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> payload = new LinkedHashMap<>();

  @NotNull
  @Column(nullable = false)
  private Instant createdAt;

  @NotNull
  @Column(nullable = false)
  private Instant updatedAt;

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
    if (this.nextAttemptAt == null) {
      this.nextAttemptAt = now;
    }
    if (this.state == null) {
      this.state = JobState.PENDING;
    }
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }
}
