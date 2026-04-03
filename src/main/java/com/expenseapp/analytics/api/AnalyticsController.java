package com.expenseapp.analytics.api;

import com.expenseapp.analytics.dto.AnalyticsResponse;
import com.expenseapp.analytics.dto.SpendingByCategoryResponse;
import com.expenseapp.analytics.service.AnalyticsService;
import com.expenseapp.shared.dto.ApiResponse;
import com.expenseapp.user.service.UserService;
import com.expenseapp.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for analytics and reporting operations.
 */
@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Analytics and reporting APIs")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserService userService;

    public AnalyticsController(AnalyticsService analyticsService, UserService userService) {
        this.analyticsService = analyticsService;
        this.userService = userService;
    }

    /**
     * Get comprehensive analytics for the authenticated user.
     *
     * @param startDate start date for analytics (optional)
     * @param endDate end date for analytics (optional)
     * @param authentication the current authentication
     * @return AnalyticsResponse
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user analytics", description = "Retrieves comprehensive analytics for the authenticated user")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getUserAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        AnalyticsResponse response = analyticsService.getUserAnalytics(user, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success("Analytics retrieved successfully", response));
    }

    /**
     * Get spending breakdown by category.
     *
     * @param startDate start date for analysis (optional)
     * @param endDate end date for analysis (optional)
     * @param authentication the current authentication
     * @return List of SpendingByCategoryResponse
     */
    @GetMapping("/spending-by-category")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get spending by category", description = "Retrieves spending breakdown by category")
    public ResponseEntity<ApiResponse<List<SpendingByCategoryResponse>>> getSpendingByCategory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        List<SpendingByCategoryResponse> response = analyticsService.getSpendingByCategory(user, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success("Spending by category retrieved successfully", response));
    }

    /**
     * Get current month summary.
     *
     * @param authentication the current authentication
     * @return Map with income, expenses, and balance
     */
    @GetMapping("/current-month")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current month summary", description = "Retrieves financial summary for the current month")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getCurrentMonthSummary(
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        Map<String, BigDecimal> response = analyticsService.getCurrentMonthSummary(user);

        return ResponseEntity.ok(ApiResponse.success("Current month summary retrieved successfully", response));
    }

    /**
     * Get year-to-date summary.
     *
     * @param authentication the current authentication
     * @return Map with income, expenses, and balance
     */
    @GetMapping("/year-to-date")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get year-to-date summary", description = "Retrieves financial summary for the year to date")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getYearToDateSummary(
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        Map<String, BigDecimal> response = analyticsService.getYearToDateSummary(user);

        return ResponseEntity.ok(ApiResponse.success("Year-to-date summary retrieved successfully", response));
    }
}