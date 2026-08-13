package me.danielx.api.risk;

import me.danielx.api.transactions.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
  Optional<RiskAssessment> findFirstByTransactionIdOrderByScoredAtDesc(Long transactionId);

  List<RiskAssessment> findByTransactionIdIn(Collection<Long> transactionIds);

  Optional<RiskAssessment> findByTransactionAndContentHash(Transaction transaction, String contentHash);
}
