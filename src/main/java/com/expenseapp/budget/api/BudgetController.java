package com.expenseapp.budget.api;

import com.expenseapp.budget.dto.BudgetRequest;
import com.expenseapp.budget.dto.BudgetResponse;
import com.expenseapp.budget.service.BudgetService;
import com.expenseapp.shared.dto.ApiResponse;
import com.expenseapp.user.service.UserService;
import com.expenseapp.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for budget management operations.
 */
@RestController
@RequestMapping("/api/budgets")
@Tag(name = "Budgets", description = "Budget management APIs")
public class BudgetController {

    private final BudgetService budgetService;
    private final UserService userService;

    public BudgetController(BudgetService budgetService, UserService userService) {
        this.budgetService = budgetService;
        this.userService = userService;
    }

    /**
     * Create a new budget.
     *
     * @param request the budget request
     * @param authentication the current authentication
     * @return BudgetResponse
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create budget", description = "Creates a new budget for the authenticated user")
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
            @Valid @RequestBody BudgetRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        BudgetResponse response = budgetService.createBudget(user, request);
        response.setUser(userService.getUserByEmail(email)); // Set user in response

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Budget created successfully", response));
    }

    /**
     * Get all budgets for the authenticated user.
     *
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort criteria
     * @param authentication the current authentication
     * @return Page of BudgetResponse
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user budgets", description = "Retrieves paginated budgets for the authenticated user")
    public ResponseEntity<ApiResponse<Page<BudgetResponse>>> getUserBudgets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        Sort sortBy = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortBy);
        Page<BudgetResponse> budgets = budgetService.getBudgetsByUser(user, pageable);

        // Set user in each response
        budgets.forEach(budget -> budget.setUser(userService.getUserByEmail(email)));

        return ResponseEntity.ok(ApiResponse.success("Budgets retrieved successfully", budgets));
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
     * Get budget by ID.
     *
     * @param id the budget ID
     * @param authentication the current authentication
     * @return BudgetResponse
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get budget by ID", description = "Retrieves a specific budget by ID")
    public ResponseEntity<ApiResponse<BudgetResponse>> getBudgetById(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        BudgetResponse response = budgetService.getBudgetById(user, id);
        response.setUser(userService.getUserByEmail(email));

        return ResponseEntity.ok(ApiResponse.success("Budget retrieved successfully", response));
    }

    /**
     * Update an existing budget.
     *
     * @param id the budget ID
     * @param request the budget request
     * @param authentication the current authentication
     * @return BudgetResponse
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update budget", description = "Updates an existing budget")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        BudgetResponse response = budgetService.updateBudget(user, id, request);
        response.setUser(userService.getUserByEmail(email));

        return ResponseEntity.ok(ApiResponse.success("Budget updated successfully", response));
    }

    /**
     * Delete a budget.
     *
     * @param id the budget ID
     * @param authentication the current authentication
     * @return ApiResponse
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete budget", description = "Deletes a budget")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        budgetService.deleteBudget(user, id);

        return ResponseEntity.ok(ApiResponse.success("Budget deleted successfully", null));
    }

    /**
     * Check if budget threshold is exceeded for a category.
     *
     * @param categoryId the category ID
     * @param authentication the current authentication
     * @return ApiResponse with threshold exceeded status
     */
    @GetMapping("/check-threshold/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check budget threshold", description = "Checks if budget threshold is exceeded for a category")
    public ResponseEntity<ApiResponse<Boolean>> checkBudgetThreshold(
            @PathVariable Long categoryId,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        boolean thresholdExceeded = budgetService.isBudgetThresholdExceeded(user, categoryId);

        return ResponseEntity.ok(ApiResponse.success("Budget threshold check completed", thresholdExceeded));
    }
}