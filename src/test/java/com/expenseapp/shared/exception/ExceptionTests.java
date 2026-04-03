package com.expenseapp.shared.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for custom exception classes.
 */
class ExceptionTests {

    @Test
    void testResourceNotFoundExceptionWithMessage() {
        String message = "Resource not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testResourceNotFoundExceptionWithMessageAndCause() {
        String message = "Resource not found";
        Throwable cause = new RuntimeException("Underlying cause");
        
        ResourceNotFoundException exception = new ResourceNotFoundException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("Underlying cause", exception.getCause().getMessage());
    }

    @Test
    void testResourceNotFoundExceptionIsRuntimeException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Test");
        
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testValidationExceptionWithMessage() {
        String message = "Validation failed";
        ValidationException exception = new ValidationException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testValidationExceptionWithMessageAndCause() {
        String message = "Validation failed";
        Throwable cause = new IllegalArgumentException("Invalid input");
        
        ValidationException exception = new ValidationException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("Invalid input", exception.getCause().getMessage());
    }

    @Test
    void testValidationExceptionIsRuntimeException() {
        ValidationException exception = new ValidationException("Test");
        
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testResourceNotFoundExceptionCanBeThrown() {
        assertThrows(ResourceNotFoundException.class, () -> {
            throw new ResourceNotFoundException("User not found");
        });
    }

    @Test
    void testValidationExceptionCanBeThrown() {
        assertThrows(ValidationException.class, () -> {
            throw new ValidationException("Invalid email format");
        });
    }
}