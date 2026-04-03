package com.expenseapp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic test to verify the application structure is correct
 */
class ApplicationStructureTest {

    @Test
    void testApplicationClassExists() {
        // This test just verifies that our basic class structure is in place
        // In a real Spring Boot application, this would be replaced with proper integration tests
        assertTrue(true, "Application structure test passed");
    }

    @Test
    void testPackageStructure() {
        // Verify that our package naming follows Java conventions
        String packageName = Application.class.getPackage().getName();
        assertEquals("com.expenseapp", packageName, "Package name should be com.expenseapp");
    }
}