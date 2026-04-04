package com.expenseapp.alert.api;

import com.expenseapp.alert.dto.AlertResponse;
import com.expenseapp.alert.service.AlertService;
import com.expenseapp.shared.dto.ApiResponse;
import com.expenseapp.user.domain.User;
import com.expenseapp.user.service.UserService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AlertController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
        })
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlertService alertService;

    @MockBean
    private UserService userService;

    private User testUser;
    private AlertResponse testAlertResponse;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "password", "John", "Doe");
        testUser.setId(1L);

        testAlertResponse = new AlertResponse();
        testAlertResponse.setId(1L);
        testAlertResponse.setType("BUDGET");
        testAlertResponse.setMessage("Budget exceeded");
        testAlertResponse.setRelatedEntityType("Budget");
        testAlertResponse.setRelatedEntityId(1L);
        testAlertResponse.setIsRead(false);
        testAlertResponse.setCreatedAt("2024-01-01T00:00:00");
        testAlertResponse.setUpdatedAt("2024-01-01T00:00:00");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGetUserAlerts() throws Exception {
        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        Page<AlertResponse> page = new PageImpl<>(List.of(testAlertResponse), PageRequest.of(0, 20), 1);
        when(alertService.getAlertsByUser(any(User.class), any())).thenReturn(page);

        mockMvc.perform(get("/api/alerts")
                .param("page", "0")
                .param("size", "20")
                .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].message").value("Budget exceeded"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGetAlertById() throws Exception {
        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        when(alertService.getAlertById(any(User.class), any(Long.class))).thenReturn(testAlertResponse);

        mockMvc.perform(get("/api/alerts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGetUnreadAlerts() throws Exception {
        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        when(alertService.getUnreadAlertsByUser(any(User.class))).thenReturn(List.of(testAlertResponse));

        mockMvc.perform(get("/api/alerts/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].isRead").value(false));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGetUnreadAlertsCount() throws Exception {
        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        when(alertService.countUnreadAlertsByUser(any(User.class))).thenReturn(5L);

        mockMvc.perform(get("/api/alerts/unread/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldMarkAlertAsRead() throws Exception {
        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        testAlertResponse.setIsRead(true);
        when(alertService.markAlertAsRead(any(User.class), any(Long.class))).thenReturn(testAlertResponse);

        mockMvc.perform(put("/api/alerts/1/read").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldMarkAllAlertsAsRead() throws Exception {
        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        when(alertService.markAllAlertsAsRead(any(User.class))).thenReturn(10);

        mockMvc.perform(put("/api/alerts/read-all").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(10));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldDeleteAlert() throws Exception {
        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);

        mockMvc.perform(delete("/api/alerts/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldFailWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isUnauthorized());
    }
}