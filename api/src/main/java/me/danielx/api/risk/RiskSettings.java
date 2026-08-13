package me.danielx.api.risk;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import me.danielx.api.users.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "risk_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskSettings {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @NotNull
  @Column(nullable = false)
  private int alertThreshold;

  @NotNull
  @Column(nullable = false)
  private int lowMax;

  @NotNull
  @Column(nullable = false)
  private int mediumMax;

  @NotNull
  @Column(nullable = false)
  private int configVersion;

  @NotNull
  @Column(nullable = false)
  private int engineVersion;

  @Builder.Default
  @OneToMany(mappedBy = "settings", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RiskFactorConfig> factorConfigs = new ArrayList<>();

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
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }

  public void replaceFactorConfigs(List<RiskFactorConfig> configs) {
    this.factorConfigs.clear();
    for (RiskFactorConfig config : configs) {
      config.setSettings(this);
      this.factorConfigs.add(config);
    }
  }
}
