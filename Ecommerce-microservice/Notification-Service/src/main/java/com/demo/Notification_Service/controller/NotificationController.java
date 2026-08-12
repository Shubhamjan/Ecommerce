package com.demo.Notification_Service.controller;

import com.demo.Notification_Service.dto.NotificationDto;
import com.demo.Notification_Service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    // GET /notifications/my — get my notifications
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<NotificationDto>> getMyNotifications(
            @RequestHeader("X-User_Id")String userId,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "10")int size
    ){
        return ResponseEntity.ok(notificationService.getMyNotifications(userId, PageRequest.of(
        page,size, Sort.by("createdAt").descending())));
    }

    // GET /notifications/{id} — get single notification
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<NotificationDto> getNotificationById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                notificationService.getNotificationById(id));
    }

    // ── ADMIN Endpoints ────────────────────────────────

    // GET /notifications — get all notifications
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationDto>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                notificationService.getAllNotifications(
                        PageRequest.of(page, size,
                                Sort.by("createdAt").descending())));
    }
}
