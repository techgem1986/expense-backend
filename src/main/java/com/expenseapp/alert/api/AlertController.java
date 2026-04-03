package com.expenseapp.alert.api;

import com.expenseapp.alert.dto.AlertResponse;
import com.expenseapp.alert.service.AlertService;
import com.expenseapp.shared.dto.ApiResponse;
import com.expenseapp.user.service.UserService;
import com.expenseapp.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for alert management operations.
 */
@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alerts", description = "Alert management APIs")
public class AlertController {

    private final AlertService alertService;
    private final UserService userService;

    public AlertController(AlertService alertService, UserService userService) {
        this.alertService = alertService;
        this.userService = userService;
    }

    /**
     * Get all alerts for the authenticated user.
     *
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort criteria
     * @param authentication the current authentication
     * @return Page of AlertResponse
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user alerts", description = "Retrieves paginated alerts for the authenticated user")
    public ResponseEntity<ApiResponse<Page<AlertResponse>>> getUserAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        Sort sortBy = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortBy);
        Page<AlertResponse> alerts = alertService.getAlertsByUser(user, pageable);

        return ResponseEntity.ok(ApiResponse.success("Alerts retrieved successfully", alerts));
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] parts = sort.split(",");
        if (parts.length == 2) {
            try {
                Sort.Direction direction = Sort.Direction.fromString(parts[1].trim());
                return Sort.by(direction, parts[0].trim());
            } catch (IllegalArgumentException ex) {
                // Fall back to default sort if invalid direction is provided
            }
        }

        // If invalid format, use default
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }

    /**
     * Get alert by ID.
     *
     * @param id the alert ID
     * @param authentication the current authentication
     * @return AlertResponse
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get alert by ID", description = "Retrieves a specific alert by ID")
    public ResponseEntity<ApiResponse<AlertResponse>> getAlertById(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        AlertResponse response = alertService.getAlertById(user, id);

        return ResponseEntity.ok(ApiResponse.success("Alert retrieved successfully", response));
    }

    /**
     * Get unread alerts for the authenticated user.
     *
     * @param authentication the current authentication
     * @return List of AlertResponse
     */
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get unread alerts", description = "Retrieves unread alerts for the authenticated user")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getUnreadAlerts(
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        List<AlertResponse> alerts = alertService.getUnreadAlertsByUser(user);

        return ResponseEntity.ok(ApiResponse.success("Unread alerts retrieved successfully", alerts));
    }

    /**
     * Get count of unread alerts for the authenticated user.
     *
     * @param authentication the current authentication
     * @return Count of unread alerts
     */
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get unread alerts count", description = "Retrieves count of unread alerts for the authenticated user")
    public ResponseEntity<ApiResponse<Long>> getUnreadAlertsCount(
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        long count = alertService.countUnreadAlertsByUser(user);

        return ResponseEntity.ok(ApiResponse.success("Unread alerts count retrieved successfully", count));
    }

    /**
     * Mark alert as read.
     *
     * @param id the alert ID
     * @param authentication the current authentication
     * @return AlertResponse
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark alert as read", description = "Marks a specific alert as read")
    public ResponseEntity<ApiResponse<AlertResponse>> markAlertAsRead(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        AlertResponse response = alertService.markAlertAsRead(user, id);

        return ResponseEntity.ok(ApiResponse.success("Alert marked as read", response));
    }

    /**
     * Mark all alerts as read for the authenticated user.
     *
     * @param authentication the current authentication
     * @return Number of alerts marked as read
     */
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark all alerts as read", description = "Marks all alerts as read for the authenticated user")
    public ResponseEntity<ApiResponse<Integer>> markAllAlertsAsRead(
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        int count = alertService.markAllAlertsAsRead(user);

        return ResponseEntity.ok(ApiResponse.success("All alerts marked as read", count));
    }

    /**
     * Delete an alert.
     *
     * @param id the alert ID
     * @param authentication the current authentication
     * @return ApiResponse
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete alert", description = "Deletes a specific alert")
    public ResponseEntity<ApiResponse<Void>> deleteAlert(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        alertService.deleteAlert(user, id);

        return ResponseEntity.ok(ApiResponse.success("Alert deleted successfully", null));
    }
}