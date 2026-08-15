package me.danielx.api.notifications;

import java.util.UUID;

public class NotificationNotFoundException extends RuntimeException {
  public NotificationNotFoundException(UUID notificationId) {
    super("Notification " + notificationId + " was not found");
  }
}
