package com.expenseapp.service;

import com.expenseapp.recurring.domain.RecurringTransaction;
import com.expenseapp.recurring.repository.RecurringTransactionRepository;
import com.expenseapp.recurring.service.RecurringTransactionService;
import com.expenseapp.shared.exception.ResourceNotFoundException;
import com.expenseapp.shared.exception.ValidationException;
import com.expenseapp.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.expenseapp.recurring.domain.RecurringTransaction.Frequency.MONTHLY;
import static com.expenseapp.recurring.domain.RecurringTransaction.TransactionType.EXPENSE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionServiceTest {

    @Mock
    private RecurringTransactionRepository recurringTransactionRepository;

    @InjectMocks
    private RecurringTransactionService recurringTransactionService;

    private User testUser;
    private RecurringTransaction testRecurringTransaction;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "password", "John", "Doe");
        testUser.setId(1L);

        testRecurringTransaction = new RecurringTransaction();
        testRecurringTransaction.setId(1L);
        testRecurringTransaction.setUser(testUser);
        testRecurringTransaction.setName("Monthly Rent");
        testRecurringTransaction.setAmount(new BigDecimal("1500.00"));
        testRecurringTransaction.setType(EXPENSE);
        testRecurringTransaction.setFrequency(MONTHLY);
        testRecurringTransaction.setDayOfMonth(1);
        testRecurringTransaction.setStartDate(LocalDate.now().withDayOfMonth(1));
        testRecurringTransaction.setIsActive(true);
    }

    @Test
    void shouldCreateRecurringTransaction() {
        when(recurringTransactionRepository.save(any(RecurringTransaction.class))).thenReturn(testRecurringTransaction);

        RecurringTransaction created = recurringTransactionService.createRecurringTransaction(testRecurringTransaction);

        assertNotNull(created);
        assertEquals("Monthly Rent", created.getName());
        verify(recurringTransactionRepository).save(any(RecurringTransaction.class));
    }

    @Test
    void shouldThrowExceptionWhenAmountIsZero() {
        testRecurringTransaction.setAmount(BigDecimal.ZERO);

        assertThrows(ValidationException.class, () -> {
            recurringTransactionService.createRecurringTransaction(testRecurringTransaction);
        });
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNegative() {
        testRecurringTransaction.setAmount(new BigDecimal("-100.00"));

        assertThrows(ValidationException.class, () -> {
            recurringTransactionService.createRecurringTransaction(testRecurringTransaction);
        });
    }

    @Test
    void shouldThrowExceptionWhenTypeIsNull() {
        testRecurringTransaction.setType(null);

        assertThrows(ValidationException.class, () -> {
            recurringTransactionService.createRecurringTransaction(testRecurringTransaction);
        });
    }

    @Test
    void shouldThrowExceptionWhenStartDateIsNull() {
        testRecurringTransaction.setStartDate(null);

        assertThrows(ValidationException.class, () -> {
            recurringTransactionService.createRecurringTransaction(testRecurringTransaction);
        });
    }

    @Test
    void shouldThrowExceptionWhenDayOfMonthIsInvalid() {
        testRecurringTransaction.setDayOfMonth(0);

        assertThrows(ValidationException.class, () -> {
            recurringTransactionService.createRecurringTransaction(testRecurringTransaction);
        });
    }

    @Test
    void shouldThrowExceptionWhenDayOfMonthIsGreaterThan31() {
        testRecurringTransaction.setDayOfMonth(32);

        assertThrows(ValidationException.class, () -> {
            recurringTransactionService.createRecurringTransaction(testRecurringTransaction);
        });
    }

    @Test
    void shouldThrowExceptionWhenUserIsNull() {
        testRecurringTransaction.setUser(null);

        assertThrows(ValidationException.class, () -> {
            recurringTransactionService.createRecurringTransaction(testRecurringTransaction);
        });
    }

    @Test
    void shouldThrowExceptionWhenEndDateIsBeforeStartDate() {
        testRecurringTransaction.setEndDate(LocalDate.now().minusDays(10));

        assertThrows(ValidationException.class, () -> {
            recurringTransactionService.createRecurringTransaction(testRecurringTransaction);
        });
    }

    @Test
    void shouldGetRecurringTransactionById() {
        when(recurringTransactionRepository.findByIdWithCategory(1L)).thenReturn(Optional.of(testRecurringTransaction));

        RecurringTransaction found = recurringTransactionService.getRecurringTransactionById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void shouldThrowExceptionWhenRecurringTransactionNotFound() {
        when(recurringTransactionRepository.findByIdWithCategory(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            recurringTransactionService.getRecurringTransactionById(1L);
        });
    }

    @Test
    void shouldGetActiveRecurringTransactionsByUser() {
        when(recurringTransactionRepository.findByUserAndIsActiveTrueOrderByNextExecutionDateAsc(testUser))
                .thenReturn(List.of(testRecurringTransaction));

        List<RecurringTransaction> transactions = recurringTransactionService.getActiveRecurringTransactionsByUser(testUser);

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
    }

    @Test
    void shouldGetRecurringTransactionsByUser() {
        when(recurringTransactionRepository.findByUserWithCategoryOrderByCreatedAtDesc(testUser))
                .thenReturn(List.of(testRecurringTransaction));

        List<RecurringTransaction> transactions = recurringTransactionService.getRecurringTransactionsByUser(testUser);

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
    }

    @Test
    void shouldGetDueRecurringTransactions() {
        when(recurringTransactionRepository.findByNextExecutionDateLessThanEqualAndIsActiveTrue(any(LocalDate.class)))
                .thenReturn(List.of(testRecurringTransaction));

        List<RecurringTransaction> due = recurringTransactionService.getDueRecurringTransactions(LocalDate.now());

        assertNotNull(due);
        assertEquals(1, due.size());
    }

    @Test
    void shouldUpdateRecurringTransaction() {
        when(recurringTransactionRepository.findByIdWithCategory(1L)).thenReturn(Optional.of(testRecurringTransaction));
        when(recurringTransactionRepository.save(any(RecurringTransaction.class))).thenReturn(testRecurringTransaction);

        RecurringTransaction updated = recurringTransactionService.updateRecurringTransaction(1L, testRecurringTransaction);

        assertNotNull(updated);
        verify(recurringTransactionRepository).save(any(RecurringTransaction.class));
    }

    @Test
    void shouldDeactivateRecurringTransaction() {
        when(recurringTransactionRepository.findByIdWithCategory(1L)).thenReturn(Optional.of(testRecurringTransaction));
        when(recurringTransactionRepository.save(any(RecurringTransaction.class))).thenReturn(testRecurringTransaction);

        recurringTransactionService.deactivateRecurringTransaction(1L);

        assertFalse(testRecurringTransaction.getIsActive());
        verify(recurringTransactionRepository).save(any(RecurringTransaction.class));
    }

    @Test
    void shouldUpdateNextExecutionDate() {
        when(recurringTransactionRepository.findByIdWithCategory(1L)).thenReturn(Optional.of(testRecurringTransaction));
        when(recurringTransactionRepository.save(any(RecurringTransaction.class))).thenReturn(testRecurringTransaction);

        LocalDate newDate = LocalDate.now().plusDays(30);
        recurringTransactionService.updateNextExecutionDate(1L, newDate);

        assertEquals(newDate, testRecurringTransaction.getNextExecutionDate());
        verify(recurringTransactionRepository).save(any(RecurringTransaction.class));
    }

    @Test
    void shouldDeleteRecurringTransaction() {
        when(recurringTransactionRepository.findByIdWithCategory(1L)).thenReturn(Optional.of(testRecurringTransaction));

        recurringTransactionService.deleteRecurringTransaction(1L);

        verify(recurringTransactionRepository).delete(testRecurringTransaction);
    }
}