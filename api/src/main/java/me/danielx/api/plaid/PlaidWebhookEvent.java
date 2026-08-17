package me.danielx.api.plaid;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "plaid_webhook_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaidWebhookEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Builder.Default
  @NotNull
  @Column(nullable = false, unique = true, updatable = false)
  private UUID publicId = UUID.randomUUID();

  @Column(length = 255)
  private String providerEventId;

  @NotNull
  @Column(nullable = false, unique = true, length = 64)
  private String dedupeHash;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "plaid_item_id")
  private PlaidItem plaidItem;

  @NotNull
  @Column(nullable = false, length = 64)
  private String webhookType;

  @NotNull
  @Column(nullable = false, length = 64)
  private String webhookCode;

  @Builder.Default
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> payload = new LinkedHashMap<>();

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
