package me.danielx.api.transactions;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import me.danielx.api.accounts.Account;
import me.danielx.api.users.User;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Builder.Default
  @NotNull
  @Column(nullable = false, unique = true, updatable = false)
  private UUID publicId = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @NotNull
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private SourceType sourceType;

  @NotNull
  @Column(nullable = false, updatable = false)
  private String externalId;

  @NotNull
  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;

  @NotNull
  @Length(max = 3)
  @Column(nullable = false, length = 3)
  private String currencyCode;

  @NotNull
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @NotNull
  @Column(nullable = false)
  private Instant updatedAt;

  @PrePersist
  private void onCreate() {
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  @PreUpdate
  private void onUpdate() {
    this.updatedAt = Instant.now();
  }
}
