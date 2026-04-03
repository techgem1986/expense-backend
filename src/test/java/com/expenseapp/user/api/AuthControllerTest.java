package com.expenseapp.user.api;

import com.expenseapp.shared.dto.ApiResponse;
import com.expenseapp.user.domain.User;
import com.expenseapp.user.dto.AuthResponse;
import com.expenseapp.user.dto.UserLoginRequest;
import com.expenseapp.user.dto.UserRegistrationRequest;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController.
 */
@WebMvcTest(value = AuthController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
        },
        properties = "app.jpa.auditing=false")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User testUser;
    private UserResponse testUserResponse;
    private UserRegistrationRequest registrationRequest;
    private UserLoginRequest loginRequest;
    private AuthResponse testAuthResponse;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "hashedPassword", "John", "Doe");
        testUser.setId(1L);
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());

        testUserResponse = new UserResponse(
                testUser.getId(),
                testUser.getEmail(),
                testUser.getFirstName(),
                testUser.getLastName(),
                testUser.getIsActive(),
                testUser.getCreatedAt(),
                testUser.getUpdatedAt()
        );

        registrationRequest = new UserRegistrationRequest(
                "test@example.com",
                "password123",
                "John",
                "Doe"
        );

        loginRequest = new UserLoginRequest(
                "test@example.com",
                "password123"
        );

        testAuthResponse = new AuthResponse("jwtToken", testUserResponse);
    }

    @Test
    @WithMockUser(roles = {"ANONYMOUS"})
    void shouldRegisterUserSuccessfully() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(testAuthResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwtToken"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.user.firstName").value("John"))
                .andExpect(jsonPath("$.data.user.lastName").value("Doe"));
    }

    @Test
    @WithMockUser(roles = {"ANONYMOUS"})
    void shouldReturnErrorWhenEmailAlreadyExists() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new com.expenseapp.shared.exception.ValidationException("Email already exists"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = {"ANONYMOUS"})
    void shouldLoginUserSuccessfully() throws Exception {
        // Given
        when(userService.authenticateUser(any(UserLoginRequest.class))).thenReturn(testAuthResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwtToken"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(roles = {"ANONYMOUS"})
    void shouldReturnErrorForInvalidCredentials() throws Exception {
        // Given
        when(userService.authenticateUser(any(UserLoginRequest.class)))
                .thenThrow(new com.expenseapp.shared.exception.ValidationException("Invalid email or password"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = {"ANONYMOUS"})
    void shouldReturnErrorForInvalidRegistrationRequest() throws Exception {
        // Given - empty email
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest(
                "",
                "password123",
                "John",
                "Doe"
        );

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ANONYMOUS"})
    void shouldReturnErrorForInvalidLoginRequest() throws Exception {
        // Given - empty email
        UserLoginRequest invalidRequest = new UserLoginRequest(
                "",
                "password123"
        );

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ANONYMOUS"})
    void shouldReturnErrorForMissingPasswordInRegistration() throws Exception {
        // Given
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest(
                "test@example.com",
                "",
                "John",
                "Doe"
        );

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ANONYMOUS"})
    void shouldReturnErrorForMissingPasswordInLogin() throws Exception {
        // Given
        UserLoginRequest invalidRequest = new UserLoginRequest(
                "test@example.com",
                ""
        );

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}