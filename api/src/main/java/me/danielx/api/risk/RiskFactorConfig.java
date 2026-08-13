package me.danielx.api.risk;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(
    name = "risk_factor_configs",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_risk_factor_configs_settings_key",
            columnNames = {"settings_id", "factor_key"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskFactorConfig {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "settings_id", nullable = false)
  private RiskSettings settings;

  @NotNull
  @Column(nullable = false, length = 64)
  private String factorKey;

  @NotNull
  @Column(nullable = false)
  private boolean enabled;

  @NotNull
  @Column(nullable = false)
  private int maxPoints;

  @Builder.Default
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> parameters = new LinkedHashMap<>();

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
    if (this.parameters == null) {
      this.parameters = new LinkedHashMap<>();
    }
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }
}
