package com.expenseapp.user.service;

import com.expenseapp.shared.exception.ResourceNotFoundException;
import com.expenseapp.shared.exception.ValidationException;
import com.expenseapp.shared.security.JwtService;
import com.expenseapp.user.domain.User;
import com.expenseapp.user.dto.AuthResponse;
import com.expenseapp.user.dto.UserLoginRequest;
import com.expenseapp.user.dto.UserRegistrationRequest;
import com.expenseapp.user.dto.UserResponse;
import com.expenseapp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRegistrationRequest registrationRequest;
    private UserLoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "hashedPassword", "John", "Doe");
        testUser.setId(1L);
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());

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
    }

    @Test
    void testRegisterUserSuccess() {
        // Given
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(testUser.getEmail(), testUser.getId())).thenReturn("jwtToken");

        // When
        AuthResponse response = userService.registerUser(registrationRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("jwtToken", response.getToken());
        assertNotNull(response.getUser());
        assertEquals("test@example.com", response.getUser().getEmail());
        assertEquals("John", response.getUser().getFirstName());
        assertEquals("Doe", response.getUser().getLastName());

        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(testUser.getEmail(), testUser.getId());
    }

    @Test
    void testRegisterUserWhenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(true);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.registerUser(registrationRequest);
        });

        assertTrue(exception.getMessage().contains("already exists"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAuthenticateUserSuccess() {
        // Given
        when(userRepository.findByEmailAndIsActiveTrue(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(testUser.getEmail(), testUser.getId())).thenReturn("jwtToken");

        // When
        AuthResponse response = userService.authenticateUser(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        assertNotNull(response.getUser());
        assertEquals("test@example.com", response.getUser().getEmail());

        verify(jwtService).generateToken(testUser.getEmail(), testUser.getId());
    }

    @Test
    void testAuthenticateUserWithInvalidEmail() {
        // Given
        when(userRepository.findByEmailAndIsActiveTrue(loginRequest.getEmail())).thenReturn(Optional.empty());

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.authenticateUser(loginRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testAuthenticateUserWithInvalidPassword() {
        // Given
        when(userRepository.findByEmailAndIsActiveTrue(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash())).thenReturn(false);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.authenticateUser(loginRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testAuthenticateUserWithInactiveUser() {
        // Given
        testUser.setIsActive(false);
        when(userRepository.findByEmailAndIsActiveTrue(loginRequest.getEmail())).thenReturn(Optional.empty());

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.authenticateUser(loginRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testGetUserByIdSuccess() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        UserResponse response = userService.getUserById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
    }

    @Test
    void testGetUserByIdNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(999L);
        });

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void testGetUserByEmailSuccess() {
        // Given
        when(userRepository.findByEmailAndIsActiveTrue("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserResponse response = userService.getUserByEmail("test@example.com");

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void testGetUserByEmailNotFound() {
        // Given
        when(userRepository.findByEmailAndIsActiveTrue("notfound@example.com")).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserByEmail("notfound@example.com");
        });

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void testGetUserEntityByEmailSuccess() {
        // Given
        when(userRepository.findByEmailAndIsActiveTrue("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserEntityByEmail("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals(testUser, result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void testGetUserEntityByEmailNotFound() {
        // Given
        when(userRepository.findByEmailAndIsActiveTrue("notfound@example.com")).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserEntityByEmail("notfound@example.com");
        });

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void testMapToUserResponse() {
        // This tests the internal mapping method through the public API
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getFirstName(), response.getFirstName());
        assertEquals(testUser.getLastName(), response.getLastName());
        assertEquals(testUser.getIsActive(), response.getIsActive());
        assertEquals(testUser.getCreatedAt(), response.getCreatedAt());
    }

    @Test
    void testRegisterUserSetsIsActiveToTrue() {
        // Given
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertTrue(savedUser.getIsActive());
            return testUser;
        });
        when(jwtService.generateToken(anyString(), anyLong())).thenReturn("jwtToken");

        // When
        userService.registerUser(registrationRequest);

        // Then verified in the answer
    }
}