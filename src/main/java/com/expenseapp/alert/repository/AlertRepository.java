package com.expenseapp.alert.repository;

import com.expenseapp.alert.domain.Alert;
import com.expenseapp.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Alert entity operations.
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /**
     * Find all alerts for a user ordered by creation date.
     */
    List<Alert> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find alerts for a user with pagination.
     */
    Page<Alert> findByUser(User user, Pageable pageable);

    /**
     * Find unread alerts for a user.
     */
    List<Alert> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    /**
     * Count unread alerts for a user.
     */
    long countByUserAndIsReadFalse(User user);

    /**
     * Find alerts by type for a user.
     */
    List<Alert> findByUserAndTypeOrderByCreatedAtDesc(User user, String type);

    /**
     * Mark all alerts as read for a user.
     */
    @Modifying
    @Query("UPDATE Alert a SET a.isRead = true, a.readAt = CURRENT_TIMESTAMP WHERE a.user = :user AND a.isRead = false")
    int markAllAsReadByUser(@Param("user") User user);

    /**
     * Delete read alerts older than specified days.
     */
    @Modifying
    @Query("DELETE FROM Alert a WHERE a.isRead = true AND a.createdAt < :cutoffDate")
    int deleteOldReadAlerts(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}