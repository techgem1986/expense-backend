package com.expenseapp.analytics.api;

import com.expenseapp.analytics.dto.AnalyticsResponse;
import com.expenseapp.analytics.dto.SpendingByCategoryResponse;
import com.expenseapp.analytics.service.AnalyticsService;
import com.expenseapp.shared.dto.ApiResponse;
import com.expenseapp.user.domain.User;
import com.expenseapp.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AnalyticsController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
        })
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private UserService userService;

    private User testUser;
    private AnalyticsResponse testAnalyticsResponse;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "password", "John", "Doe");
        testUser.setId(1L);

        testAnalyticsResponse = new AnalyticsResponse(
                new BigDecimal("10000.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("5000.00"),
                List.of(new SpendingByCategoryResponse("Food", new BigDecimal("1000.00"), 20.0)),
                List.of(),
                LocalDate.now().minusMonths(6),
                LocalDate.now()
        );
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGetUserAnalytics() throws Exception {
        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        when(analyticsService.getUserAnalytics(any(User.class), any(), any())).thenReturn(testAnalyticsResponse);

        mockMvc.perform(get("/api/analytics")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalIncome").value(10000.00));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGetUserAnalyticsWithoutDates() throws Exception {
        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        when(analyticsService.getUserAnalytics(any(User.class), any(), any())).thenReturn(testAnalyticsResponse);

        mockMvc.perform(get("/api/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGetSpendingByCategory() throws Exception {
        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        when(analyticsService.getSpendingByCategory(any(User.class), any(), any())).thenReturn(
                List.of(new SpendingByCategoryResponse("Food", new BigDecimal("1000.00"), 20.0))
        );

        mockMvc.perform(get("/api/analytics/spending-by-category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].categoryName").value("Food"))
                .andExpect(jsonPath("$.data[0].totalAmount").value(1000.00));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGetCurrentMonthSummary() throws Exception {
        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        when(analyticsService.getCurrentMonthSummary(any(User.class))).thenReturn(
                Map.of("income", new BigDecimal("5000.00"), "expenses", new BigDecimal("3000.00"), "balance", new BigDecimal("2000.00"))
        );

        mockMvc.perform(get("/api/analytics/current-month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.income").value(5000.00))
                .andExpect(jsonPath("$.data.expenses").value(3000.00))
                .andExpect(jsonPath("$.data.balance").value(2000.00));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGetYearToDateSummary() throws Exception {
        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        when(analyticsService.getYearToDateSummary(any(User.class))).thenReturn(
                Map.of("income", new BigDecimal("30000.00"), "expenses", new BigDecimal("18000.00"), "balance", new BigDecimal("12000.00"))
        );

        mockMvc.perform(get("/api/analytics/year-to-date"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.income").value(30000.00))
                .andExpect(jsonPath("$.data.expenses").value(18000.00));
    }

    @Test
    void shouldFailWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/analytics"))
                .andExpect(status().isUnauthorized());
    }
}