package com.expenseapp.budget.service;

import com.expenseapp.budget.domain.Budget;
import com.expenseapp.budget.domain.Budget.BudgetPeriod;
import com.expenseapp.budget.domain.BudgetCategory;
import com.expenseapp.budget.dto.BudgetRequest;
import com.expenseapp.budget.dto.BudgetResponse;
import com.expenseapp.budget.repository.BudgetRepository;
import com.expenseapp.category.domain.Category;
import com.expenseapp.category.service.CategoryService;
import com.expenseapp.shared.exception.ResourceNotFoundException;
import com.expenseapp.shared.exception.ValidationException;
import com.expenseapp.transaction.service.TransactionService;
import com.expenseapp.user.domain.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for budget-related business logic.
 */
@Service
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryService categoryService;
    private final TransactionService transactionService;

    public BudgetService(BudgetRepository budgetRepository,
                        CategoryService categoryService,
                        TransactionService transactionService) {
        this.budgetRepository = budgetRepository;
        this.categoryService = categoryService;
        this.transactionService = transactionService;
    }

    /**
     * Create a new budget.
     */
    @Caching(evict = {
            @CacheEvict(value = "budgetById", allEntries = true),
            @CacheEvict(value = "budgetsByUser", allEntries = true)
    })
    public BudgetResponse createBudget(User user, BudgetRequest request) {
        validateBudgetRequest(user, request);

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setName(request.getName());
        budget.setLimitAmount(request.getLimitAmount());
        budget.setPeriod(request.getPeriod());
        budget.setAlertThreshold(request.getAlertThreshold() != null ?
            request.getAlertThreshold() : BigDecimal.valueOf(0.80));
        budget.setStartDate(request.getStartDate());

        // Save budget first to get ID
        Budget savedBudget = budgetRepository.save(budget);

        // Add categories if provided
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            for (Long categoryId : request.getCategoryIds()) {
                Category category = categoryService.getCategoryEntityById(categoryId);
                BudgetCategory budgetCategory = new BudgetCategory(savedBudget, category, null);
                savedBudget.getBudgetCategories().add(budgetCategory);
            }
        }

        // Calculate current spending
        BigDecimal currentSpent = calculateCurrentSpending(savedBudget);

        return mapToBudgetResponse(savedBudget, currentSpent);
    }

    /**
     * Get budget by ID.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "budgetById", key = "#budgetId")
    public BudgetResponse getBudgetById(User user, Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + budgetId));

        // Security check: ensure user owns the budget
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new ValidationException("Access denied");
        }

        BigDecimal currentSpent = calculateCurrentSpending(budget);
        return mapToBudgetResponse(budget, currentSpent);
    }

    /**
     * Get all budgets for a user.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "budgetsByUser", key = "#user.id")
    public List<BudgetResponse> getBudgetsByUser(User user) {
        List<Budget> budgets = budgetRepository.findByUserOrderByCreatedAtDesc(user);
        return budgets.stream()
                .map(budget -> {
                    BigDecimal currentSpent = calculateCurrentSpending(budget);
                    return mapToBudgetResponse(budget, currentSpent);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get budgets for a user with pagination.
     */
    @Transactional(readOnly = true)
    public Page<BudgetResponse> getBudgetsByUser(User user, Pageable pageable) {
        Page<Budget> budgets = budgetRepository.findByUser(user, pageable);
        return budgets.map(budget -> {
            BigDecimal currentSpent = calculateCurrentSpending(budget);
            return mapToBudgetResponse(budget, currentSpent);
        });
    }

    /**
     * Update an existing budget.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "budgetById", key = "#budgetId"),
            @CacheEvict(value = "budgetsByUser", allEntries = true),
            @CacheEvict(value = "budgetPages", allEntries = true)
    })
    public BudgetResponse updateBudget(User user, Long budgetId, BudgetRequest request) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + budgetId));

        // Security check: ensure user owns the budget
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new ValidationException("Access denied");
        }

        validateBudgetRequestForUpdate(user, request, budgetId);

        budget.setName(request.getName());
        budget.setLimitAmount(request.getLimitAmount());
        budget.setPeriod(request.getPeriod());
        if (request.getAlertThreshold() != null) {
            budget.setAlertThreshold(request.getAlertThreshold());
        }
        budget.setStartDate(request.getStartDate());

        // Update categories
        budget.getBudgetCategories().clear();
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            for (Long categoryId : request.getCategoryIds()) {
                Category category = categoryService.getCategoryEntityById(categoryId);
                BudgetCategory budgetCategory = new BudgetCategory(budget, category, null);
                budget.getBudgetCategories().add(budgetCategory);
            }
        }

        Budget updatedBudget = budgetRepository.save(budget);
        BigDecimal currentSpent = calculateCurrentSpending(updatedBudget);
        return mapToBudgetResponse(updatedBudget, currentSpent);
    }

    /**
     * Delete a budget.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "budgetById", key = "#budgetId"),
            @CacheEvict(value = "budgetsByUser", allEntries = true),
            @CacheEvict(value = "budgetPages", allEntries = true)
    })
    public void deleteBudget(User user, Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + budgetId));

        // Security check: ensure user owns the budget
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new ValidationException("Access denied");
        }

        budgetRepository.delete(budget);
    }

    /**
     * Check if budget threshold is exceeded and should trigger alert.
     */
    @Transactional(readOnly = true)
    public boolean isBudgetThresholdExceeded(User user, Long categoryId) {
        List<Budget> budgets = budgetRepository.findActiveBudgetsByUser(user, LocalDate.now());

        for (Budget budget : budgets) {
            // Check if category is included in budget
            boolean categoryIncluded = budget.getBudgetCategories().isEmpty() || // All categories
                    budget.getBudgetCategories().stream()
                            .anyMatch(bc -> bc.getCategory().getId().equals(categoryId));

            if (categoryIncluded) {
                BigDecimal currentSpent = calculateCurrentSpending(budget);
                BigDecimal thresholdAmount = budget.getLimitAmount().multiply(budget.getAlertThreshold());
                if (currentSpent.compareTo(thresholdAmount) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Calculate current spending for a budget.
     */
    private BigDecimal calculateCurrentSpending(Budget budget) {
        LocalDate startDate = budget.getStartDate();
        LocalDate endDate = calculateEndDate(budget);

        List<Long> categoryIds = budget.getBudgetCategories().stream()
                .map(bc -> bc.getCategory().getId())
                .collect(Collectors.toList());

        if (categoryIds.isEmpty()) {
            // All categories
            return transactionService.calculateTotalExpensesByDateRange(budget.getUser(), startDate, endDate);
        } else {
            // Specific categories - this would need a custom query
            // For now, return zero (would need to implement category-specific spending)
            return BigDecimal.ZERO;
        }
    }

    /**
     * Calculate end date for budget period.
     */
    private LocalDate calculateEndDate(Budget budget) {
        LocalDate startDate = budget.getStartDate();
        return switch (budget.getPeriod()) {
            case MONTHLY -> startDate.plusMonths(1).minusDays(1);
            case YEARLY -> startDate.plusYears(1).minusDays(1);
        };
    }

    /**
     * Validate budget request.
     */
    private void validateBudgetRequest(User user, BudgetRequest request) {
        if (budgetRepository.existsByUserAndName(user, request.getName())) {
            throw new ValidationException("Budget with name '" + request.getName() + "' already exists");
        }

        if (request.getStartDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Start date cannot be in the future");
        }
    }

    /**
     * Validate budget request for update.
     */
    private void validateBudgetRequestForUpdate(User user, BudgetRequest request, Long excludeBudgetId) {
        Budget existing = budgetRepository.findByUserAndName(user, request.getName());
        if (existing != null && !existing.getId().equals(excludeBudgetId)) {
            throw new ValidationException("Budget with name '" + request.getName() + "' already exists");
        }
    }

    /**
     * Map budget to response with calculations.
     */
    private BudgetResponse mapToBudgetResponse(Budget budget, BigDecimal currentSpent) {
        BigDecimal limitAmount = budget.getLimitAmount();
        BigDecimal remainingAmount = limitAmount.subtract(currentSpent);

        double spentPercentage = 0.0;
        if (limitAmount != null && limitAmount.compareTo(BigDecimal.ZERO) > 0) {
            spentPercentage = currentSpent.divide(limitAmount, 4, BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        boolean isOverBudget = limitAmount != null && limitAmount.compareTo(BigDecimal.ZERO) > 0 && currentSpent.compareTo(limitAmount) > 0;

        List<Category> categories = budget.getBudgetCategories().stream()
                .map(BudgetCategory::getCategory)
                .collect(Collectors.toList());

        return new BudgetResponse(
                budget.getId(),
                null, // User will be set by controller if needed
                budget.getName(),
                limitAmount,
                budget.getPeriod(),
                budget.getAlertThreshold(),
                budget.getStartDate(),
                currentSpent,
                remainingAmount,
                spentPercentage,
                isOverBudget,
                categories.stream().map(cat -> categoryService.mapToCategoryResponse(cat)).collect(Collectors.toList()),
                budget.getCreatedAt().toString(),
                budget.getUpdatedAt().toString()
        );
    }
}