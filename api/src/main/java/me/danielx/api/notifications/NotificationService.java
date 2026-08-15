package me.danielx.api.notifications;

import io.micrometer.core.instrument.MeterRegistry;
import me.danielx.api.notifications.dto.NotificationListResponse;
import me.danielx.api.notifications.dto.UnreadCountResponse;
import me.danielx.api.risk.RiskAssessment;
import me.danielx.api.users.dto.AuthenticatedUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationService {
  private final NotificationRepository notificationRepository;
  private final MeterRegistry meterRegistry;

  public NotificationService(
      NotificationRepository notificationRepository, MeterRegistry meterRegistry) {
    this.notificationRepository = notificationRepository;
    this.meterRegistry = meterRegistry;
  }

  @Transactional
  public void createAlert(RiskAssessment assessment, NotificationType type) {
    if (notificationRepository.existsByTransactionIdAndAssessmentIdAndType(
        assessment.getTransaction().getId(), assessment.getId(), type)) {
      return;
    }
    String merchant = assessment.getTransaction().effectiveMerchant();
    Notification notification =
        Notification.builder()
            .user(assessment.getUser())
            .transaction(assessment.getTransaction())
            .assessment(assessment)
            .type(type)
            .title(type == NotificationType.SETTINGS_RESCORE
                ? "Risk score changed after settings update"
                : "Suspicious transaction flagged")
            .body(
                merchant
                    + " scored "
                    + assessment.getScore()
                    + ". "
                    + assessment.getPrimaryReason())
            .score(assessment.getScore())
            .build();
    try {
      notificationRepository.saveAndFlush(notification);
      meterRegistry.counter("noscam.notifications.alerts").increment();
    } catch (DataIntegrityViolationException ignored) {
      meterRegistry.counter("noscam.notifications.duplicates").increment();
    }
  }

  @Transactional(readOnly = true)
  public Page<NotificationListResponse> list(
      AuthenticatedUser currentUser, boolean unreadOnly, Pageable pageable) {
    meterRegistry.counter("noscam.notifications.list").increment();
    Page<Notification> page =
        unreadOnly
            ? notificationRepository.findAllByUserIdAndReadAtIsNull(currentUser.id(), pageable)
            : notificationRepository.findAllByUserId(currentUser.id(), pageable);
    return page.map(NotificationListResponse::from);
  }

  @Transactional(readOnly = true)
  public UnreadCountResponse unreadCount(AuthenticatedUser currentUser) {
    meterRegistry.counter("noscam.notifications.unread_count").increment();
    return new UnreadCountResponse(
        notificationRepository.countByUserIdAndReadAtIsNull(currentUser.id()));
  }

  @Transactional
  public NotificationListResponse markRead(AuthenticatedUser currentUser, UUID notificationId) {
    Notification notification =
        notificationRepository
            .findByPublicIdAndUserId(notificationId, currentUser.id())
            .orElseThrow(() -> new NotificationNotFoundException(notificationId));
    if (notification.getReadAt() == null) {
      notification.setReadAt(Instant.now());
    }
    return NotificationListResponse.from(notification);
  }

  @Transactional
  public void markAllRead(AuthenticatedUser currentUser) {
    notificationRepository.markAllRead(currentUser.id(), Instant.now());
  }
}
