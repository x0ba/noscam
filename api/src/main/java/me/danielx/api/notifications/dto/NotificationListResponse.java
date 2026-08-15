package me.danielx.api.notifications.dto;

import me.danielx.api.notifications.Notification;
import me.danielx.api.notifications.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationListResponse(
    UUID id,
    UUID transactionId,
    NotificationType type,
    String title,
    String body,
    int score,
    Instant readAt,
    Instant createdAt) {

  public static NotificationListResponse from(Notification notification) {
    return new NotificationListResponse(
        notification.getPublicId(),
        notification.getTransaction().getPublicId(),
        notification.getType(),
        notification.getTitle(),
        notification.getBody(),
        notification.getScore(),
        notification.getReadAt(),
        notification.getCreatedAt());
  }
}
