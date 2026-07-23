package me.danielx.api.global;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import me.danielx.api.users.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Entity
@Table(
    name = "idempotency_records",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_idempotency_user_method_endpoint_key",
          columnNames = {"user_id", "http_method", "endpoint", "idempotency_key"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(max = 255)
  @Column(nullable = false, name = "idempotency_key")
  private String idempotencyKey;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  @NotNull
  private User user;

  @NotBlank
  @Size(max = 255)
  @Column(nullable = false, length = 255)
  private String endpoint;

  @NotBlank
  @Size(min = 64, max = 64)
  @Column(name = "request_hash", nullable = false, length = 64)
  private String requestHash;

  @Enumerated(EnumType.STRING)
  @NotNull
  @Column(name = "request_status", nullable = false, length = 32)
  private RequestStatus status;

  @Enumerated(EnumType.STRING)
  @NotNull
  @Column(name = "http_method", nullable = false, length = 10)
  private HttpMethod httpMethod;

  @Min(100)
  @Max(599)
  @Column(name = "response_code")
  private Integer responseCode;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "response_body", columnDefinition = "jsonb")
  private Map<String, Object> responseBody;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "response_headers", columnDefinition = "jsonb")
  private Map<String, List<String>> responseHeaders;

  @NotNull
  @Column(nullable = false, name = "lock_expires_at")
  private Instant lockExpiresAt;

  @NotNull
  @Column(nullable = false, name = "expires_at")
  private Instant expiresAt;

  @CreationTimestamp
  @NotNull
  @Column(nullable = false, name = "created_at")
  private Instant createdAt;

  @UpdateTimestamp
  @NotNull
  @Column(nullable = false, name = "updated_at")
  private Instant updatedAt;
}
