package com.expenseapp.alert.service;

import com.expenseapp.alert.domain.Alert;
import com.expenseapp.alert.dto.AlertResponse;
import com.expenseapp.alert.repository.AlertRepository;
import com.expenseapp.shared.exception.ResourceNotFoundException;
import com.expenseapp.user.domain.User;
import com.expenseapp.user.service.UserService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for alert-related business logic.
 */
@Service
@Transactional
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserService userService;

    public AlertService(AlertRepository alertRepository, UserService userService) {
        this.alertRepository = alertRepository;
        this.userService = userService;
    }

    /**
     * Create a new alert.
     */
    @Caching(evict = {
            @CacheEvict(value = "alertsByUser", allEntries = true),
            @CacheEvict(value = "unreadAlertsByUser", allEntries = true),
            @CacheEvict(value = "unreadAlertCountByUser", allEntries = true)
    })
    public Alert createAlert(Alert alert) {
        return alertRepository.save(alert);
    }

    /**
     * Create a budget alert for a user.
     */
    public Alert createBudgetAlert(User user, String message, Long budgetId) {
        Alert alert = new Alert(user, "BUDGET", message, "Budget", budgetId);
        return createAlert(alert);
    }

    /**
     * Create a general system alert.
     */
    public Alert createSystemAlert(User user, String message) {
        Alert alert = new Alert(user, "SYSTEM", message);
        return createAlert(alert);
    }

    /**
     * Get alert by ID.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "alertById", key = "#alertId")
    public AlertResponse getAlertById(User user, Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));

        // Security check: ensure user owns the alert
        if (!alert.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        return mapToAlertResponse(alert);
    }

    /**
     * Get all alerts for a user.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "alertsByUser", key = "#user.id")
    public List<AlertResponse> getAlertsByUser(User user) {
        List<Alert> alerts = alertRepository.findByUserOrderByCreatedAtDesc(user);
        return alerts.stream()
                .map(this::mapToAlertResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get alerts for a user with pagination.
     */
    @Transactional(readOnly = true)
    public Page<AlertResponse> getAlertsByUser(User user, Pageable pageable) {
        Page<Alert> alerts = alertRepository.findByUser(user, pageable);
        return alerts.map(this::mapToAlertResponse);
    }

    /**
     * Get unread alerts for a user.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "unreadAlertsByUser", key = "#user.id")
    public List<AlertResponse> getUnreadAlertsByUser(User user) {
        List<Alert> alerts = alertRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        return alerts.stream()
                .map(this::mapToAlertResponse)
                .collect(Collectors.toList());
    }

    /**
     * Count unread alerts for a user.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "unreadAlertCountByUser", key = "#user.id")
    public long countUnreadAlertsByUser(User user) {
        return alertRepository.countByUserAndIsReadFalse(user);
    }

    /**
     * Mark alert as read.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "alertById", key = "#alertId"),
            @CacheEvict(value = "alertsByUser", allEntries = true),
            @CacheEvict(value = "alertPagesByUser", allEntries = true),
            @CacheEvict(value = "unreadAlertsByUser", allEntries = true),
            @CacheEvict(value = "unreadAlertCountByUser", allEntries = true)
    })
    public AlertResponse markAlertAsRead(User user, Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));

        // Security check: ensure user owns the alert
        if (!alert.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        alert.markAsRead();
        Alert savedAlert = alertRepository.save(alert);
        return mapToAlertResponse(savedAlert);
    }

    /**
     * Mark all alerts as read for a user.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "alertsByUser", allEntries = true),
            @CacheEvict(value = "alertPagesByUser", allEntries = true),
            @CacheEvict(value = "unreadAlertsByUser", allEntries = true),
            @CacheEvict(value = "unreadAlertCountByUser", allEntries = true),
            @CacheEvict(value = "alertById", allEntries = true)
    })
    public int markAllAlertsAsRead(User user) {
        return alertRepository.markAllAsReadByUser(user);
    }

    /**
     * Delete an alert.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "alertById", key = "#alertId"),
            @CacheEvict(value = "alertsByUser", allEntries = true),
            @CacheEvict(value = "alertPagesByUser", allEntries = true),
            @CacheEvict(value = "unreadAlertsByUser", allEntries = true),
            @CacheEvict(value = "unreadAlertCountByUser", allEntries = true)
    })
    public void deleteAlert(User user, Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));

        // Security check: ensure user owns the alert
        if (!alert.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        alertRepository.delete(alert);
    }

    /**
     * Clean up old read alerts (older than 30 days).
     */
    @Transactional
    public int cleanupOldAlerts() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        return alertRepository.deleteOldReadAlerts(cutoffDate);
    }

    /**
     * Map Alert entity to AlertResponse DTO.
     */
    private AlertResponse mapToAlertResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                userService.getUserByEmail(alert.getUser().getEmail()), // This might be inefficient
                alert.getType(),
                alert.getMessage(),
                alert.getRelatedEntityType(),
                alert.getRelatedEntityId(),
                alert.getIsRead(),
                alert.getReadAt(),
                alert.getCreatedAt().toString(),
                alert.getUpdatedAt().toString()
        );
    }
}