package com.expenseapp.user.api;

import com.expenseapp.shared.dto.ApiResponse;
import com.expenseapp.user.domain.User;
import com.expenseapp.user.dto.UserResponse;
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

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UserController.
 */
@WebMvcTest(value = UserController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
        },
        properties = "app.jpa.auditing=false")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User testUser;
    private UserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "password", "John", "Doe");
        testUser.setId(1L);
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        testUserResponse = new UserResponse(
                testUser.getId(),
                testUser.getEmail(),
                testUser.getFirstName(),
                testUser.getLastName(),
                testUser.getIsActive(),
                testUser.getCreatedAt(),
                testUser.getUpdatedAt()
        );
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGetCurrentUserProfile() throws Exception {
        // Given
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUserResponse);

        // When & Then
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGetUserById() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(testUserResponse);

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldReturnNotFoundForNonExistentUser() throws Exception {
        // Given
        when(userService.getUserById(999L)).thenThrow(
                new com.expenseapp.shared.exception.ResourceNotFoundException("User not found")
        );

        // When & Then
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldFailWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "other@example.com")
    void shouldReturnErrorForUnauthorizedUserAccess() throws Exception {
        // Given - trying to access another user's profile without admin role
        when(userService.getUserById(1L)).thenThrow(
                new org.springframework.security.access.AccessDeniedException("Access denied")
        );

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isForbidden());
    }
}