package com.expenseapp.shared.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ApiResponse DTO.
 */
class ApiResponseTest {

    @Test
    void testDefaultConstructor() {
        ApiResponse<String> response = new ApiResponse<>();
        
        assertFalse(response.isSuccess());
        assertNull(response.getMessage());
        assertNull(response.getData());
        assertNull(response.getTimestamp());
        assertNull(response.getPath());
    }

    @Test
    void testParameterizedConstructor() {
        LocalDateTime now = LocalDateTime.now();
        ApiResponse<String> response = new ApiResponse<>(true, "Success", "test data", now, "/api/test");
        
        assertTrue(response.isSuccess());
        assertEquals("Success", response.getMessage());
        assertEquals("test data", response.getData());
        assertEquals(now, response.getTimestamp());
        assertEquals("/api/test", response.getPath());
    }

    @Test
    void testSetters() {
        ApiResponse<String> response = new ApiResponse<>();
        
        response.setSuccess(true);
        response.setMessage("Test message");
        response.setData("Test data");
        response.setTimestamp(LocalDateTime.now());
        response.setPath("/test/path");
        
        assertTrue(response.isSuccess());
        assertEquals("Test message", response.getMessage());
        assertEquals("Test data", response.getData());
        assertNotNull(response.getTimestamp());
        assertEquals("/test/path", response.getPath());
    }

    @Test
    void testStaticSuccessMethodWithData() {
        ApiResponse<String> response = ApiResponse.success("test data");
        
        assertTrue(response.isSuccess());
        assertNull(response.getMessage());
        assertEquals("test data", response.getData());
        assertNotNull(response.getTimestamp());
        assertNull(response.getPath());
    }

    @Test
    void testStaticSuccessMethodWithMessageAndData() {
        ApiResponse<String> response = ApiResponse.success("Operation completed", "test data");
        
        assertTrue(response.isSuccess());
        assertEquals("Operation completed", response.getMessage());
        assertEquals("test data", response.getData());
        assertNotNull(response.getTimestamp());
        assertNull(response.getPath());
    }

    @Test
    void testStaticErrorMethod() {
        ApiResponse<String> response = ApiResponse.error("Error occurred");
        
        assertFalse(response.isSuccess());
        assertEquals("Error occurred", response.getMessage());
        assertNull(response.getData());
        assertNotNull(response.getTimestamp());
        assertNull(response.getPath());
    }

    @Test
    void testStaticErrorMethodWithData() {
        Map<String, String> errors = Map.of("field", "error message");
        ApiResponse<Map<String, String>> response = ApiResponse.error("Validation failed", errors);
        
        assertFalse(response.isSuccess());
        assertEquals("Validation failed", response.getMessage());
        assertEquals(errors, response.getData());
        assertNotNull(response.getTimestamp());
        assertNull(response.getPath());
    }

    @Test
    void testEquality() {
        LocalDateTime now = LocalDateTime.now();
        ApiResponse<String> response1 = new ApiResponse<>(true, "Success", "data", now, "/path");
        ApiResponse<String> response2 = new ApiResponse<>(true, "Success", "data", now, "/path");
        
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testInequality() {
        ApiResponse<String> response1 = ApiResponse.success("data1");
        ApiResponse<String> response2 = ApiResponse.success("data2");
        
        assertNotEquals(response1, response2);
    }

    @Test
    void testToString() {
        ApiResponse<String> response = ApiResponse.success("test data");
        String toString = response.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("ApiResponse"));
        assertTrue(toString.contains("success=true"));
        assertTrue(toString.contains("data=test data"));
    }

    @Test
    void testEqualityWithNullFields() {
        ApiResponse<String> response1 = new ApiResponse<>();
        ApiResponse<String> response2 = new ApiResponse<>();
        
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testEqualityWithDifferentSuccessValues() {
        ApiResponse<String> response1 = ApiResponse.success("data");
        ApiResponse<String> response2 = ApiResponse.error("error");
        
        assertNotEquals(response1, response2);
    }
}