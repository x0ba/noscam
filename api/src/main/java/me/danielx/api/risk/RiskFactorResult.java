package me.danielx.api.risk;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(
    name = "risk_factor_results",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_risk_factor_results_assessment_key",
            columnNames = {"assessment_id", "factor_key"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskFactorResult {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "assessment_id", nullable = false)
  private RiskAssessment assessment;

  @NotNull
  @Column(nullable = false, length = 64)
  private String factorKey;

  @NotNull
  @Column(nullable = false)
  private int points;

  @NotNull
  @Column(nullable = false)
  private boolean matched;

  @NotNull
  @Column(nullable = false, columnDefinition = "text")
  private String explanation;

  @Builder.Default
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> evidence = new LinkedHashMap<>();
}
