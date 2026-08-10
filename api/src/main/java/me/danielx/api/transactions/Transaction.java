package me.danielx.api.transactions;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import me.danielx.api.common.accounts.Account;
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

  @Column(updatable = false)
  private String externalId;

  @NotNull
  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;

  @NotNull
  @Length(max = 3)
  @Column(nullable = false, length = 3)
  private String currencyCode;

  @Column(length = 255)
  private String merchant;

  @Column(length = 255)
  private String displayName;

  @Column(columnDefinition = "text")
  private String originalDescription;

  private Instant authorizedAt;

  private Instant postedAt;

  @NotNull
  @Builder.Default
  @Column(nullable = false)
  private boolean pending = false;

  @Column(length = 128)
  private String pendingTransactionId;

  @Column(length = 255)
  private String category;

  @Column(length = 64)
  private String paymentChannel;

  @Column(length = 2)
  private String isoCurrencyCountry;

  @Column(length = 2)
  private String merchantCountry;

  @NotNull
  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private TransactionStatus status = TransactionStatus.ACTIVE;

  private Instant removedAt;

  @Column(length = 64)
  private String contentHash;

  @NotNull
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @NotNull
  @Column(nullable = false)
  private Instant updatedAt;

  @PrePersist
  private void onCreate() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
    if (this.postedAt == null) {
      this.postedAt = now;
    }
    if (this.status == null) {
      this.status = TransactionStatus.ACTIVE;
    }
  }

  @PreUpdate
  private void onUpdate() {
    this.updatedAt = Instant.now();
  }

  public String effectiveMerchant() {
    if (merchant != null && !merchant.isBlank()) {
      return merchant;
    }
    if (displayName != null && !displayName.isBlank()) {
      return displayName;
    }
    if (originalDescription != null && !originalDescription.isBlank()) {
      return originalDescription;
    }
    return "Unknown merchant";
  }

  public Instant effectiveDate() {
    if (postedAt != null) {
      return postedAt;
    }
    if (authorizedAt != null) {
      return authorizedAt;
    }
    return createdAt;
  }
}
