package me.danielx.api.notifications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  Page<Notification> findAllByUserId(Long userId, Pageable pageable);

  Page<Notification> findAllByUserIdAndReadAtIsNull(Long userId, Pageable pageable);

  long countByUserIdAndReadAtIsNull(Long userId);

  Optional<Notification> findByPublicIdAndUserId(UUID publicId, Long userId);

  boolean existsByTransactionIdAndAssessmentIdAndType(
      Long transactionId, Long assessmentId, NotificationType type);

  @Modifying
  @Query(
      "update Notification n set n.readAt = :readAt where n.user.id = :userId and n.readAt is null")
  int markAllRead(@Param("userId") Long userId, @Param("readAt") Instant readAt);
}
