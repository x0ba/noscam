package me.danielx.api.risk;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import me.danielx.api.transactions.Transaction;
import me.danielx.api.users.User;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
    name = "risk_assessments",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_risk_assessments_transaction_hash",
            columnNames = {"transaction_id", "content_hash"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Builder.Default
  @NotNull
  @Column(nullable = false, unique = true, updatable = false)
  private UUID publicId = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "transaction_id", nullable = false)
  private Transaction transaction;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @NotNull
  @Column(nullable = false)
  private int score;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private RiskLevel riskLevel;

  @NotNull
  @Column(nullable = false, columnDefinition = "text")
  private String primaryReason;

  @NotNull
  @Column(nullable = false)
  private int engineVersion;

  @NotNull
  @Column(nullable = false)
  private int configVersion;

  @Builder.Default
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> configSnapshot = new LinkedHashMap<>();

  @NotNull
  @Column(nullable = false, length = 64)
  private String contentHash;

  @NotNull
  @Column(nullable = false)
  private Instant scoredAt;

  @NotNull
  @Column(nullable = false)
  private Instant createdAt;

  @Builder.Default
  @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RiskFactorResult> factorResults = new ArrayList<>();

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    this.createdAt = now;
    if (this.scoredAt == null) {
      this.scoredAt = now;
    }
  }
}
