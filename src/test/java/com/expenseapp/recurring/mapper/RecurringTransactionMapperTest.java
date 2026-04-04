package com.expenseapp.recurring.mapper;

import com.expenseapp.recurring.domain.RecurringTransaction;
import com.expenseapp.recurring.dto.RecurringTransactionRequest;
import com.expenseapp.recurring.dto.RecurringTransactionResponse;
import com.expenseapp.recurring.domain.RecurringTransaction.TransactionType;
import com.expenseapp.recurring.domain.RecurringTransaction.Frequency;
import com.expenseapp.user.domain.User;
import com.expenseapp.category.domain.Category;
import com.expenseapp.account.domain.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RecurringTransactionMapper.
 */
class RecurringTransactionMapperTest {

    private RecurringTransactionMapper mapper;
    private User testUser;
    private Category testCategory;
    private Account testFromAccount;
    private Account testToAccount;

    @BeforeEach
    void setUp() {
        mapper = RecurringTransactionMapper.INSTANCE;
        
        testUser = new User("test@example.com", "password", "John", "Doe");
        testUser.setId(1L);
        
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory.setDescription("Test description");
        testCategory.setType(com.expenseapp.category.domain.Category.CategoryType.EXPENSE);
        // Category user relationship not supported in entity
        testCategory.setId(1L);
        
        testFromAccount = new Account(testUser, "From Account", com.expenseapp.account.domain.AccountType.CHECKING, BigDecimal.valueOf(1000));
        testFromAccount.setId(1L);
        
        testToAccount = new Account(testUser, "To Account", com.expenseapp.account.domain.AccountType.SAVINGS, BigDecimal.valueOf(500));
        testToAccount.setId(2L);
    }

    @Test
    void testToResponseMapsNextExecutionDateCorrectly() {
        // Given
        RecurringTransaction recurringTransaction = new RecurringTransaction(
            testUser,
            testCategory,
            "Test Recurring",
            BigDecimal.valueOf(100.00),
            TransactionType.EXPENSE,
            "Test description",
            Frequency.MONTHLY,
            15,
            LocalDate.of(2024, 1, 15),
            null
        );
        recurringTransaction.setId(1L);
        
        // When
        RecurringTransactionResponse response = mapper.toResponse(recurringTransaction);
        
        // Then
        assertNotNull(response);
        assertEquals(recurringTransaction.getId(), response.getId());
        assertEquals(recurringTransaction.getName(), response.getName());
        assertEquals(recurringTransaction.getAmount(), response.getAmount());
        assertEquals(recurringTransaction.getType(), response.getType());
        assertEquals(recurringTransaction.getFrequency(), response.getFrequency());
        assertEquals(recurringTransaction.getDayOfMonth(), response.getDayOfMonth());
        assertEquals(recurringTransaction.getStartDate(), response.getStartDate());
        assertEquals(recurringTransaction.getNextExecutionDate(), response.getNextExecutionDate());
        assertNotNull(response.getNextExecutionDate());
    }

    @Test
    void testToResponseWithUserAndCategoryMapsNextExecutionDateCorrectly() {
        // Given
        RecurringTransaction recurringTransaction = new RecurringTransaction(
            testUser,
            testCategory,
            "Test Recurring",
            BigDecimal.valueOf(100.00),
            TransactionType.EXPENSE,
            "Test description",
            Frequency.MONTHLY,
            15,
            LocalDate.of(2024, 1, 15),
            null
        );
        recurringTransaction.setId(1L);
        
        // When
        RecurringTransactionResponse response = mapper.toResponseWithUserAndCategory(recurringTransaction, testUser, testCategory);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getUser());
        assertNotNull(response.getCategory());
        assertEquals(testUser.getId(), response.getUser().getId());
        assertEquals(testCategory.getId(), response.getCategory().getId());
        assertEquals(recurringTransaction.getNextExecutionDate(), response.getNextExecutionDate());
        assertNotNull(response.getNextExecutionDate());
    }

    @Test
    void testToResponseWithAccountsMapsNextExecutionDateCorrectly() {
        // Given
        RecurringTransaction recurringTransaction = new RecurringTransaction(
            testUser,
            testCategory,
            "Test Recurring",
            BigDecimal.valueOf(100.00),
            TransactionType.EXPENSE,
            "Test description",
            Frequency.MONTHLY,
            15,
            LocalDate.of(2024, 1, 15),
            null
        );
        recurringTransaction.setId(1L);
        
        // When
        RecurringTransactionResponse response = mapper.toResponseWithAccounts(
            recurringTransaction, 
            testUser, 
            testCategory, 
            testFromAccount, 
            testToAccount
        );
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getUser());
        assertNotNull(response.getCategory());
        assertNotNull(response.getFromAccount());
        assertNotNull(response.getToAccount());
        assertEquals(testUser.getId(), response.getUser().getId());
        assertEquals(testCategory.getId(), response.getCategory().getId());
        assertEquals(testFromAccount.getId(), response.getFromAccount().getId());
        assertEquals(testToAccount.getId(), response.getToAccount().getId());
        assertEquals(recurringTransaction.getNextExecutionDate(), response.getNextExecutionDate());
        assertNotNull(response.getNextExecutionDate());
    }

    @Test
    void testToEntityIgnoresNextExecutionDate() {
        // Given
        RecurringTransactionRequest request = new RecurringTransactionRequest();
        request.setName("Test Recurring");
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setType(TransactionType.EXPENSE);
        request.setFrequency(Frequency.MONTHLY);
        request.setDayOfMonth(15);
        request.setStartDate(LocalDate.of(2024, 1, 15));
        
        // When
        RecurringTransaction entity = mapper.toEntity(request);
        
        // Then
        assertNotNull(entity);
        assertEquals(request.getName(), entity.getName());
        assertEquals(request.getAmount(), entity.getAmount());
        assertEquals(request.getType(), entity.getType());
        assertEquals(request.getFrequency(), entity.getFrequency());
        assertEquals(request.getDayOfMonth(), entity.getDayOfMonth());
        assertEquals(request.getStartDate(), entity.getStartDate());
        // NextExecutionDate should be calculated by the entity, not mapped from request
        assertNotNull(entity.getNextExecutionDate());
    }

    @Test
    void testNextExecutionDateIsNotNullAfterMapping() {
        // Given
        RecurringTransaction recurringTransaction = new RecurringTransaction(
            testUser,
            testCategory,
            "Test Recurring",
            BigDecimal.valueOf(100.00),
            TransactionType.EXPENSE,
            "Test description",
            Frequency.MONTHLY,
            15,
            LocalDate.of(2024, 1, 15),
            null
        );
        recurringTransaction.setId(1L);
        
        // When
        RecurringTransactionResponse response = mapper.toResponse(recurringTransaction);
        
        // Then
        assertNotNull(response.getNextExecutionDate(), "NextExecutionDate should not be null after mapping");
    }
}