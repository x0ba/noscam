package me.danielx.api.plaid;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import me.danielx.api.users.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "plaid_items",
    uniqueConstraints = @UniqueConstraint(name = "uk_plaid_items_plaid_item_id", columnNames = "plaid_item_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaidItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Builder.Default
  @NotNull
  @Column(nullable = false, unique = true, updatable = false)
  private UUID publicId = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  private User user;

  @NotNull
  @Column(nullable = false, length = 128)
  private String plaidItemId;

  @NotNull
  @Column(nullable = false, columnDefinition = "text")
  private String accessToken;

  @Column(length = 64)
  private String institutionId;

  @Column(length = 255)
  private String institutionName;

  @Column(columnDefinition = "text")
  private String cursor;

  @NotNull
  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private PlaidItemStatus status = PlaidItemStatus.ACTIVE;

  private Instant consentExpiresAt;

  @NotNull
  @Column(nullable = false)
  private Instant connectedAt;

  private Instant lastSuccessfulSync;

  @Column(length = 128)
  private String lastErrorCode;

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
    if (this.connectedAt == null) {
      this.connectedAt = now;
    }
    if (this.status == null) {
      this.status = PlaidItemStatus.ACTIVE;
    }
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }
}
