package me.danielx.api.notifications;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import me.danielx.api.risk.RiskAssessment;
import me.danielx.api.transactions.Transaction;
import me.danielx.api.users.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "notifications",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_notifications_txn_assessment_type",
            columnNames = {"transaction_id", "assessment_id", "type"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

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

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "transaction_id", nullable = false)
  private Transaction transaction;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "assessment_id", nullable = false)
  private RiskAssessment assessment;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private NotificationType type;

  @NotNull
  @Column(nullable = false, length = 255)
  private String title;

  @NotNull
  @Column(nullable = false, columnDefinition = "text")
  private String body;

  @NotNull
  @Column(nullable = false)
  private int score;

  private Instant readAt;

  @NotNull
  @Column(nullable = false)
  private Instant createdAt;

  @PrePersist
  void onCreate() {
    if (this.createdAt == null) {
      this.createdAt = Instant.now();
    }
  }
}
