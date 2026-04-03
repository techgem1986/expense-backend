package com.expenseapp.user.api;

import com.expenseapp.shared.dto.ApiResponse;
import com.expenseapp.user.dto.AuthResponse;
import com.expenseapp.user.dto.UserLoginRequest;
import com.expenseapp.user.dto.UserRegistrationRequest;
import com.expenseapp.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Register a new user.
     *
     * @param request the registration request
     * @return AuthResponse with JWT token and user info
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account and returns JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody UserRegistrationRequest request) {
        AuthResponse response = userService.registerUser(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", response));
    }

    /**
     * Authenticate a user.
     *
     * @param request the login request
     * @return AuthResponse with JWT token and user info
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user credentials and returns JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody UserLoginRequest request) {
        AuthResponse response = userService.authenticateUser(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}