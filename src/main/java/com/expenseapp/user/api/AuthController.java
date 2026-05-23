package com.expenseapp.user.api;

import com.expenseapp.shared.dto.ApiResponse;
import com.expenseapp.shared.security.JwtService;
import com.expenseapp.user.dto.AuthResponse;
import com.expenseapp.user.dto.UserLoginRequest;
import com.expenseapp.user.dto.UserRegistrationRequest;
import com.expenseapp.user.dto.UserResponse;
import com.expenseapp.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * REST controller for authentication operations.
 * Sets JWT tokens as httpOnly cookies for improved security (prevents XSS token theft).
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * Register a new user.
     * Sets JWT as an httpOnly cookie in addition to returning it in the response body
     * for backward compatibility with clients that haven't migrated to cookie-based auth.
     *
     * @param request the registration request
     * @return AuthResponse with JWT token and user info
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account and returns JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody UserRegistrationRequest request) {
        AuthResponse response = userService.registerUser(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createJwtCookie(response.getToken()).toString())
                .body(ApiResponse.success("User registered successfully", response));
    }

    /**
     * Authenticate a user.
     * Sets JWT as an httpOnly cookie in addition to returning it in the response body.
     *
     * @param request the login request
     * @return AuthResponse with JWT token and user info
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user credentials and returns JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody UserLoginRequest request) {
        AuthResponse response = userService.authenticateUser(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createJwtCookie(response.getToken()).toString())
                .body(ApiResponse.success("Login successful", response));
    }

    /**
     * Refresh the JWT token by reading the current token from the httpOnly cookie
     * and issuing a new one with a fresh expiration.
     *
     * @return AuthResponse with new JWT token and user info
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token", description = "Refreshes the JWT token using the current cookie-based token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = "token", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No token found in cookie"));
        }

        // Validate the existing token
        String username = jwtService.extractUsername(token);
        if (username == null || !jwtService.isTokenValid(token, username)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid or expired token"));
        }

        // Generate a new token with fresh expiration
        Long userId = jwtService.extractUserId(token);
        String newToken = jwtService.generateToken(username, userId);

        // Create response without user object (session is already established)
        AuthResponse response = new AuthResponse(newToken, null);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createJwtCookie(newToken).toString())
                .body(ApiResponse.success("Token refreshed successfully", response));
    }

    /**
     * Logout the user by clearing the JWT cookie.
     *
     * @return ApiResponse
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Clears the JWT cookie to log out the user")
    public ResponseEntity<ApiResponse<Void>> logout() {
        ResponseCookie clearCookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(ApiResponse.success("Logged out successfully", null));
    }

    /**
     * Create an httpOnly JWT cookie for secure token transport.
     */
    private ResponseCookie createJwtCookie(String token) {
        return ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();
    }
}
