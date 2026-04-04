package com.expenseapp.service;

import com.expenseapp.account.domain.Account;
import com.expenseapp.account.repository.AccountRepository;
import com.expenseapp.category.domain.Category;
import com.expenseapp.shared.exception.ResourceNotFoundException;
import com.expenseapp.shared.exception.ValidationException;
import com.expenseapp.transaction.domain.Transaction;
import com.expenseapp.transaction.repository.TransactionRepository;
import com.expenseapp.transaction.service.TransactionService;
import com.expenseapp.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Transaction testTransaction;
    private Account testAccount;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "password", "John", "Doe");
        testUser.setId(1L);

        testCategory = new Category("Food", "Food expenses", Category.CategoryType.EXPENSE);
        testCategory.setId(1L);

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setUser(testUser);
        testAccount.setName("Checking Account");
        testAccount.setCurrentBalance(new BigDecimal("5000.00"));

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setUser(testUser);
        testTransaction.setCategory(testCategory);
        testTransaction.setAmount(new BigDecimal("100.00"));
        testTransaction.setType(Transaction.TransactionType.EXPENSE);
        testTransaction.setDescription("Test transaction");
        testTransaction.setTransactionDate(LocalDate.now());
    }

    @Test
    void shouldCreateTransaction() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        Transaction created = transactionService.createTransaction(testTransaction);

        assertNotNull(created);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenAmountIsZero() {
        testTransaction.setAmount(BigDecimal.ZERO);

        assertThrows(ValidationException.class, () -> {
            transactionService.createTransaction(testTransaction);
        });
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNegative() {
        testTransaction.setAmount(new BigDecimal("-100.00"));

        assertThrows(ValidationException.class, () -> {
            transactionService.createTransaction(testTransaction);
        });
    }

    @Test
    void shouldThrowExceptionWhenTypeIsNull() {
        testTransaction.setType(null);

        assertThrows(ValidationException.class, () -> {
            transactionService.createTransaction(testTransaction);
        });
    }

    @Test
    void shouldThrowExceptionWhenDateIsNull() {
        testTransaction.setTransactionDate(null);

        assertThrows(ValidationException.class, () -> {
            transactionService.createTransaction(testTransaction);
        });
    }

    @Test
    void shouldThrowExceptionWhenUserIsNull() {
        testTransaction.setUser(null);

        assertThrows(ValidationException.class, () -> {
            transactionService.createTransaction(testTransaction);
        });
    }

    @Test
    void shouldGetTransactionById() {
        when(transactionRepository.findByIdWithCategoryAndAccounts(1L)).thenReturn(Optional.of(testTransaction));

        Transaction found = transactionService.getTransactionById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFound() {
        when(transactionRepository.findByIdWithCategoryAndAccounts(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.getTransactionById(1L);
        });
    }

    @Test
    void shouldGetTransactionsByUser() {
        when(transactionRepository.findByUserOrderByTransactionDateDesc(testUser)).thenReturn(List.of(testTransaction));

        List<Transaction> transactions = transactionService.getTransactionsByUser(testUser);

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
    }

    @Test
    void shouldGetTransactionsByUserWithPagination() {
        Page<Transaction> transactionPage = new PageImpl<>(List.of(testTransaction), PageRequest.of(0, 20), 1);
        when(transactionRepository.findByUserWithCategoryAndAccounts(testUser, PageRequest.of(0, 20))).thenReturn(transactionPage);

        Page<Transaction> transactions = transactionService.getTransactionsByUser(testUser, PageRequest.of(0, 20));

        assertNotNull(transactions);
        assertEquals(1, transactions.getContent().size());
    }

    @Test
    void shouldGetTransactionsByUserAndType() {
        when(transactionRepository.findByUserAndTypeOrderByTransactionDateDesc(testUser, Transaction.TransactionType.EXPENSE)).thenReturn(List.of(testTransaction));

        List<Transaction> transactions = transactionService.getTransactionsByUserAndType(testUser, Transaction.TransactionType.EXPENSE);

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
    }

    @Test
    void shouldGetTransactionsByUserAndCategory() {
        when(transactionRepository.findByUserAndCategoryOrderByTransactionDateDesc(testUser, testCategory)).thenReturn(List.of(testTransaction));

        List<Transaction> transactions = transactionService.getTransactionsByUserAndCategory(testUser, testCategory);

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
    }

    @Test
    void shouldGetTransactionsByUserAndDateRange() {
        when(transactionRepository.findByUserAndTransactionDateBetweenOrderByTransactionDateDesc(any(), any(), any())).thenReturn(List.of(testTransaction));

        List<Transaction> transactions = transactionService.getTransactionsByUserAndDateRange(
                testUser, LocalDate.now().minusDays(30), LocalDate.now());

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
    }

    @Test
    void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
        assertThrows(ValidationException.class, () -> {
            transactionService.getTransactionsByUserAndDateRange(
                    testUser, LocalDate.now(), LocalDate.now().minusDays(30));
        });
    }

    @Test
    void shouldGetTransactionsByUserTypeAndDateRange() {
        when(transactionRepository.findByUserAndTypeAndTransactionDateBetweenOrderByTransactionDateDesc(any(), any(), any(), any())).thenReturn(List.of(testTransaction));

        List<Transaction> transactions = transactionService.getTransactionsByUserTypeAndDateRange(
                testUser, Transaction.TransactionType.EXPENSE, LocalDate.now().minusDays(30), LocalDate.now());

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
    }

    @Test
    void shouldGetRecurringTransactionInstancesByUser() {
        when(transactionRepository.findByUserAndIsRecurringInstanceTrueOrderByTransactionDateDesc(testUser)).thenReturn(List.of(testTransaction));

        List<Transaction> transactions = transactionService.getRecurringTransactionInstancesByUser(testUser);

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
    }

    @Test
    void shouldGetTransactionsByRecurringTransactionId() {
        when(transactionRepository.findByLinkedRecurringTransactionIdOrderByTransactionDateDesc(1L)).thenReturn(List.of(testTransaction));

        List<Transaction> transactions = transactionService.getTransactionsByRecurringTransactionId(1L);

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
    }

    @Test
    void shouldUpdateTransaction() {
        when(transactionRepository.findByIdWithCategoryAndAccounts(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        Transaction updated = transactionService.updateTransaction(1L, testTransaction);

        assertNotNull(updated);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldDeleteTransaction() {
        when(transactionRepository.findByIdWithCategoryAndAccounts(1L)).thenReturn(Optional.of(testTransaction));

        transactionService.deleteTransaction(1L);

        verify(transactionRepository).delete(testTransaction);
    }

    @Test
    void shouldCalculateTotalIncome() {
        when(transactionRepository.calculateTotalAmountByUserAndType(testUser, Transaction.TransactionType.INCOME)).thenReturn(new BigDecimal("5000.00"));

        BigDecimal income = transactionService.calculateTotalIncome(testUser);

        assertEquals(new BigDecimal("5000.00"), income);
    }

    @Test
    void shouldCalculateTotalExpenses() {
        when(transactionRepository.calculateTotalAmountByUserAndType(testUser, Transaction.TransactionType.EXPENSE)).thenReturn(new BigDecimal("3000.00"));

        BigDecimal expenses = transactionService.calculateTotalExpenses(testUser);

        assertEquals(new BigDecimal("3000.00"), expenses);
    }

    @Test
    void shouldCalculateNetBalance() {
        when(transactionRepository.calculateTotalAmountByUserAndType(testUser, Transaction.TransactionType.INCOME)).thenReturn(new BigDecimal("5000.00"));
        when(transactionRepository.calculateTotalAmountByUserAndType(testUser, Transaction.TransactionType.EXPENSE)).thenReturn(new BigDecimal("3000.00"));

        BigDecimal netBalance = transactionService.calculateNetBalance(testUser);

        assertEquals(new BigDecimal("2000.00"), netBalance);
    }

    @Test
    void shouldCalculateTotalAmountByDateRange() {
        when(transactionRepository.calculateTotalAmountByUserAndDateRange(any(), any(), any())).thenReturn(new BigDecimal("2000.00"));

        BigDecimal total = transactionService.calculateTotalAmountByDateRange(
                testUser, LocalDate.now().minusDays(30), LocalDate.now());

        assertEquals(new BigDecimal("2000.00"), total);
    }

    @Test
    void shouldCalculateTotalIncomeByDateRange() {
        when(transactionRepository.calculateTotalAmountByUserTypeAndDateRange(any(), eq(Transaction.TransactionType.INCOME), any(), any())).thenReturn(new BigDecimal("5000.00"));

        BigDecimal income = transactionService.calculateTotalIncomeByDateRange(
                testUser, LocalDate.now().minusDays(30), LocalDate.now());

        assertEquals(new BigDecimal("5000.00"), income);
    }

    @Test
    void shouldCalculateTotalExpensesByDateRange() {
        when(transactionRepository.calculateTotalAmountByUserTypeAndDateRange(any(), eq(Transaction.TransactionType.EXPENSE), any(), any())).thenReturn(new BigDecimal("3000.00"));

        BigDecimal expenses = transactionService.calculateTotalExpensesByDateRange(
                testUser, LocalDate.now().minusDays(30), LocalDate.now());

        assertEquals(new BigDecimal("3000.00"), expenses);
    }

    @Test
    void shouldCalculateSpendingByCategory() {
        Category cat = new Category("Food", "Food", Category.CategoryType.EXPENSE);
        List<Object[]> spendingData = List.<Object[]>of(new Object[]{cat, new BigDecimal("500.00")});
        when(transactionRepository.calculateTotalAmountByCategory(testUser, Transaction.TransactionType.EXPENSE)).thenReturn(spendingData);

        List<Object[]> spending = transactionService.calculateSpendingByCategory(testUser);

        assertNotNull(spending);
        assertEquals(1, spending.size());
    }

    @Test
    void shouldCalculateMonthlyIncomeAndExpenses() {
        List<Object[]> monthlyData = List.<Object[]>of(new Object[]{2024, 1, new BigDecimal("5000.00"), new BigDecimal("3000.00")});
        when(transactionRepository.calculateMonthlyIncomeAndExpenses(any(), any(), any())).thenReturn(monthlyData);

        List<Object[]> monthly = transactionService.calculateMonthlyIncomeAndExpenses(
                testUser, LocalDate.now().minusMonths(6), LocalDate.now());

        assertNotNull(monthly);
        assertEquals(1, monthly.size());
    }

    @Test
    void shouldGetTransactionsByMonthAndYear() {
        when(transactionRepository.findByUserAndMonthAndYear(testUser, 2024, 1)).thenReturn(List.of(testTransaction));

        List<Transaction> transactions = transactionService.getTransactionsByMonthAndYear(testUser, 2024, 1);

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
    }

    @Test
    void shouldCheckExistsByRecurringTransactionIdAndDate() {
        when(transactionRepository.existsByLinkedRecurringTransactionIdAndTransactionDate(1L, LocalDate.now())).thenReturn(true);

        boolean exists = transactionService.existsByRecurringTransactionIdAndDate(1L, LocalDate.now());

        assertTrue(exists);
    }

    @Test
    void shouldHandleIncomeTransaction() {
        testTransaction.setType(Transaction.TransactionType.INCOME);
        testTransaction.setToAccount(testAccount);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.saveAndFlush(any(Account.class))).thenReturn(testAccount);

        Transaction created = transactionService.createTransaction(testTransaction);

        assertNotNull(created);
        verify(accountRepository).saveAndFlush(any(Account.class));
    }

    @Test
    void shouldHandleTransferTransaction() {
        Account toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setUser(testUser);
        toAccount.setName("Savings");
        toAccount.setCurrentBalance(new BigDecimal("10000.00"));

        testTransaction.setToAccount(toAccount);
        testTransaction.setFromAccount(testAccount);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
        when(accountRepository.saveAndFlush(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction created = transactionService.createTransaction(testTransaction);

        assertNotNull(created);
        verify(accountRepository, times(2)).saveAndFlush(any(Account.class));
    }
}