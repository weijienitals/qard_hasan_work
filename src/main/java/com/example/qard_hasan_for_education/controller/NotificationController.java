package com.example.qard_hasan_for_education.controller;

import com.example.qard_hasan_for_education.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    /**
     * Get all notifications for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserNotifications(
            @PathVariable String userId,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        try {
            List<NotificationService.Notification> notifications =
                    notificationService.getUserNotifications(userId, unreadOnly);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("notifications", notifications);
            response.put("totalCount", notifications.size());
            response.put("unreadOnly", unreadOnly);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving notifications for user: {}", userId, e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error retrieving notifications"));
        }
    }

    /**
     * Mark a specific notification as read
     */
    @PostMapping("/mark-read/{notificationId}")
    public ResponseEntity<?> markNotificationAsRead(
            @PathVariable String notificationId,
            @RequestParam String userId) {
        try {
            notificationService.markNotificationAsRead(notificationId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification marked as read");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error marking notification as read: {}", notificationId, e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error updating notification"));
        }
    }

    /**
     * Mark all notifications as read for a user
     */
    @PostMapping("/mark-all-read/{userId}")
    public ResponseEntity<?> markAllNotificationsAsRead(@PathVariable String userId) {
        try {
            notificationService.markAllNotificationsAsRead(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "All notifications marked as read");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error marking all notifications as read for user: {}", userId, e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error updating notifications"));
        }
    }

    /**
     * Get notification statistics for a user
     */
    @GetMapping("/stats/{userId}")
    public ResponseEntity<?> getNotificationStats(@PathVariable String userId) {
        try {
            Map<String, Object> stats = notificationService.getNotificationStats(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving notification stats for user: {}", userId, e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error retrieving notification statistics"));
        }
    }

    /**
     * Send a payment reminder (admin function)
     */
    @PostMapping("/send-payment-reminder")
    public ResponseEntity<?> sendPaymentReminder(@RequestBody PaymentReminderRequest request) {
        try {
            notificationService.sendPaymentReminderNotification(
                    request.getStudentId(),
                    request.getLoanId(),
                    request.getAmount(),
                    request.getDueDate()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment reminder sent successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error sending payment reminder", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error sending payment reminder"));
        }
    }

    /**
     * Get unread notification count for a user
     */
    @GetMapping("/unread-count/{userId}")
    public ResponseEntity<?> getUnreadNotificationCount(@PathVariable String userId) {
        try {
            List<NotificationService.Notification> unreadNotifications =
                    notificationService.getUserNotifications(userId, true);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("unreadCount", unreadNotifications.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving unread count for user: {}", userId, e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error retrieving unread count"));
        }
    }

    /**
     * Get notifications by type for a user
     */
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<?> getNotificationsByType(
            @PathVariable String userId,
            @PathVariable String type) {
        try {
            List<NotificationService.Notification> allNotifications =
                    notificationService.getUserNotifications(userId, false);

            List<NotificationService.Notification> filteredNotifications = allNotifications.stream()
                    .filter(notification -> notification.getType().toString().equalsIgnoreCase(type))
                    .collect(java.util.stream.Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("notifications", filteredNotifications);
            response.put("type", type);
            response.put("count", filteredNotifications.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving notifications by type for user: {}", userId, e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error retrieving notifications by type"));
        }
    }

    // Request DTOs
    public static class PaymentReminderRequest {
        private String studentId;
        private String loanId;
        private String amount;
        private String dueDate;

        // Getters and setters
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }

        public String getLoanId() { return loanId; }
        public void setLoanId(String loanId) { this.loanId = loanId; }

        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }

        public String getDueDate() { return dueDate; }
        public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    }
}