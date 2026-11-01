package com.expenseapp.shared.exception;

import com.expenseapp.shared.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GlobalExceptionHandler mappings that must not fall through to
 * the generic 500 handler.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturnNotFoundForUnknownResource() {
        String resourcePath = "api/analytics/dashboard";
        NoResourceFoundException exception = new NoResourceFoundException(
                HttpMethod.GET, resourcePath, "No static resource " + resourcePath + ".");

        ResponseEntity<ApiResponse<Void>> response = handler.handleNoResourceFound(exception, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Resource not found", response.getBody().getMessage());
    }

    @Test
    void shouldReturnBadRequestForMalformedRequestBody() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "JSON parse error", new MockHttpInputMessage(new byte[0]));

        ResponseEntity<ApiResponse<Void>> response = handler.handleMessageNotReadable(exception, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Malformed request body", response.getBody().getMessage());
    }
}
