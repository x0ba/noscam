package me.danielx.api.notifications;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.danielx.api.notifications.dto.NotificationListResponse;
import me.danielx.api.notifications.dto.UnreadCountResponse;
import me.danielx.api.users.dto.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Dashboard inbox for risk alerts")
@SecurityRequirement(name = "sessionCookie")
public class NotificationController {
  private final NotificationService notificationService;

  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @GetMapping
  @Operation(summary = "List notifications, newest first")
  public ResponseEntity<Page<NotificationListResponse>> list(
      @AuthenticationPrincipal AuthenticatedUser currentUser,
      @RequestParam(defaultValue = "false") boolean unread,
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    return ResponseEntity.ok(notificationService.list(currentUser, unread, pageable));
  }

  @GetMapping("/unread-count")
  @Operation(summary = "Unread notification count for the dashboard badge")
  public ResponseEntity<UnreadCountResponse> unreadCount(
      @AuthenticationPrincipal AuthenticatedUser currentUser) {
    return ResponseEntity.ok(notificationService.unreadCount(currentUser));
  }

  @PatchMapping("/{id}/read")
  @Operation(summary = "Mark one notification as read")
  public ResponseEntity<NotificationListResponse> markRead(
      @AuthenticationPrincipal AuthenticatedUser currentUser, @PathVariable UUID id) {
    return ResponseEntity.ok(notificationService.markRead(currentUser, id));
  }

  @PostMapping("/read-all")
  @Operation(summary = "Mark all notifications as read")
  public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal AuthenticatedUser currentUser) {
    notificationService.markAllRead(currentUser);
    return ResponseEntity.noContent().build();
  }
}
