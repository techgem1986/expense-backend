package com.expenseapp.service;

import com.expenseapp.budget.domain.Budget;
import com.expenseapp.budget.domain.BudgetCategory;
import com.expenseapp.budget.dto.BudgetRequest;
import com.expenseapp.budget.dto.BudgetResponse;
import com.expenseapp.budget.repository.BudgetRepository;
import com.expenseapp.budget.service.BudgetService;
import com.expenseapp.category.domain.Category;
import com.expenseapp.category.service.CategoryService;
import com.expenseapp.shared.exception.ResourceNotFoundException;
import com.expenseapp.shared.exception.ValidationException;
import com.expenseapp.transaction.service.TransactionService;
import com.expenseapp.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.expenseapp.budget.domain.Budget.BudgetPeriod.MONTHLY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private BudgetService budgetService;

    private User testUser;
    private Budget testBudget;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "password", "John", "Doe");
        testUser.setId(1L);

        testCategory = new Category("Food", "Food expenses", Category.CategoryType.EXPENSE);
        testCategory.setId(1L);

        LocalDateTime now = LocalDateTime.now();
        testBudget = new Budget();
        testBudget.setId(1L);
        testBudget.setUser(testUser);
        testBudget.setName("Monthly Food Budget");
        testBudget.setLimitAmount(new BigDecimal("1000.00"));
        testBudget.setPeriod(MONTHLY);
        testBudget.setAlertThreshold(new BigDecimal("0.80"));
        testBudget.setStartDate(LocalDate.now().withDayOfMonth(1));
        testBudget.setCreatedAt(now);
        testBudget.setUpdatedAt(now);
        testBudget.setBudgetCategories(new ArrayList<>(List.of(new BudgetCategory(testBudget, testCategory, null))));
    }

    @Test
    void shouldThrowExceptionWhenBudgetNameExists() {
        BudgetRequest request = new BudgetRequest(
                "Monthly Food Budget",
                new BigDecimal("1000.00"),
                MONTHLY,
                new BigDecimal("0.80"),
                LocalDate.now().withDayOfMonth(1),
                null
        );

        when(budgetRepository.existsByUserAndName(any(), any())).thenReturn(true);

        assertThrows(ValidationException.class, () -> {
            budgetService.createBudget(testUser, request);
        });
    }

    @Test
    void shouldThrowExceptionWhenStartDateIsInFuture() {
        BudgetRequest request = new BudgetRequest(
                "Future Budget",
                new BigDecimal("1000.00"),
                MONTHLY,
                new BigDecimal("0.80"),
                LocalDate.now().plusDays(10),
                null
        );

        when(budgetRepository.existsByUserAndName(any(), any())).thenReturn(false);

        assertThrows(ValidationException.class, () -> {
            budgetService.createBudget(testUser, request);
        });
    }

    @Test
    void shouldThrowExceptionWhenBudgetNotFound() {
        when(budgetRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            budgetService.getBudgetById(testUser, 1L);
        });
    }

    @Test
    void shouldThrowExceptionWhenAccessingOtherUserBudget() {
        User otherUser = new User("other@example.com", "password", "Jane", "Doe");
        otherUser.setId(2L);
        testBudget.setUser(otherUser);

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));

        assertThrows(ValidationException.class, () -> {
            budgetService.getBudgetById(testUser, 1L);
        });
    }

    @Test
    void shouldDeleteBudget() {
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));

        budgetService.deleteBudget(testUser, 1L);

        verify(budgetRepository).delete(testBudget);
    }

    @Test
    void shouldThrowExceptionWhenDeletingOtherUserBudget() {
        User otherUser = new User("other@example.com", "password", "Jane", "Doe");
        otherUser.setId(2L);
        testBudget.setUser(otherUser);

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));

        assertThrows(ValidationException.class, () -> {
            budgetService.deleteBudget(testUser, 1L);
        });
    }
}