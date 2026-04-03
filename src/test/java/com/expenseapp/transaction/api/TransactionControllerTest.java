package com.expenseapp.transaction.api;

import com.expenseapp.transaction.domain.Transaction;
import com.expenseapp.transaction.dto.TransactionRequest;
import com.expenseapp.transaction.dto.TransactionResponse;
import com.expenseapp.transaction.service.TransactionService;
import com.expenseapp.transaction.mapper.TransactionMapper;
import com.expenseapp.user.service.UserService;
import com.expenseapp.user.domain.User;
import com.expenseapp.category.domain.Category;
import com.expenseapp.category.service.CategoryService;
import com.expenseapp.account.service.AccountService;
import com.expenseapp.shared.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = TransactionController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
        },
        properties = "app.jpa.auditing=false")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private UserService userService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private TransactionMapper transactionMapper;

    @MockBean
    private AccountService accountService;

    private User testUser;
    private Category testCategory;
    private Transaction testTransaction;
    private TransactionResponse testTransactionResponse;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "password", "John", "Doe");
        testUser.setId(1L);

        testCategory = new Category("Food", "Food expenses", Category.CategoryType.EXPENSE);
        testCategory.setId(1L);

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setUser(testUser);
        testTransaction.setCategory(testCategory);
        testTransaction.setAmount(new BigDecimal("100.00"));
        testTransaction.setType(Transaction.TransactionType.EXPENSE);
        testTransaction.setDescription("Test transaction");
        testTransaction.setTransactionDate(LocalDate.now());

        testTransactionResponse = new TransactionResponse();
        testTransactionResponse.setId(1L);
        testTransactionResponse.setAmount(new BigDecimal("100.00"));
        testTransactionResponse.setType(Transaction.TransactionType.EXPENSE);
        testTransactionResponse.setDescription("Test transaction");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldCreateTransaction() throws Exception {
        // Given
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100.00"),
                Transaction.TransactionType.EXPENSE,
                "Test transaction",
                LocalDate.now(),
                1L
        );

        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        when(categoryService.getCategoryEntityById(1L)).thenReturn(testCategory);
        when(transactionMapper.toEntity(any())).thenReturn(testTransaction);
        when(transactionService.createTransaction(testTransaction)).thenReturn(testTransaction);
        when(transactionMapper.toResponseWithUserAndCategory(testTransaction, testUser, testCategory))
                .thenReturn(testTransactionResponse);

        // When & Then
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.amount").value(100.00));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGetUserTransactions() throws Exception {
        // Given
        Page<TransactionResponse> page = new PageImpl<>(List.of(testTransactionResponse), PageRequest.of(0, 20), 1);

        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        when(transactionService.getTransactionsByUser(eq(testUser), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(testTransaction), PageRequest.of(0, 20), 1));
        when(transactionMapper.toResponseWithUserAndCategory(testTransaction, testUser, testCategory)).thenReturn(testTransactionResponse);

        // When & Then
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].amount").value(100.00));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGetUserTransactionsWithSortParameter() throws Exception {
        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        when(transactionService.getTransactionsByUser(eq(testUser), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(testTransaction), PageRequest.of(0, 20), 1));
        when(transactionMapper.toResponseWithUserAndCategory(testTransaction, testUser, testCategory)).thenReturn(testTransactionResponse);

        mockMvc.perform(get("/api/transactions?sort=transactionDate,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldFailWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGetTransactionById() throws Exception {
        // Given
        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        when(transactionService.getTransactionById(1L)).thenReturn(testTransaction);
        when(transactionMapper.toResponseWithUserAndCategory(testTransaction, testUser, testCategory))
                .thenReturn(testTransactionResponse);

        // When & Then
        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldUpdateTransaction() throws Exception {
        // Given
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("150.00"),
                Transaction.TransactionType.EXPENSE,
                "Updated transaction",
                LocalDate.now(),
                1L
        );

        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        when(transactionService.getTransactionById(1L)).thenReturn(testTransaction);
        when(categoryService.getCategoryEntityById(1L)).thenReturn(testCategory);
        when(transactionMapper.toEntity(any())).thenReturn(testTransaction);
        when(transactionService.updateTransaction(eq(1L), any(Transaction.class))).thenReturn(testTransaction);
        when(transactionMapper.toResponseWithUserAndCategory(testTransaction, testUser, testCategory))
                .thenReturn(testTransactionResponse);

        // When & Then
        mockMvc.perform(put("/api/transactions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldDeleteTransaction() throws Exception {
        // Given
        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        when(transactionService.getTransactionById(1L)).thenReturn(testTransaction);

        // When & Then
        mockMvc.perform(delete("/api/transactions/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}