package com.expenseapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test to verify that the Spring Boot application context loads correctly
 * This test validates our configuration, database connections, and bean wiring
 */
@SpringBootTest
@ActiveProfiles("test")
class ApplicationIntegrationTest {

    @Test
    void contextLoads() {
        // If this test passes, it means:
        // 1. All Spring beans are properly configured
        // 2. Database connection is working
        // 3. Redis connection is working
        // 4. Security configuration is valid
        // 5. Quartz scheduler is configured
        // This is our primary test for application startup
    }
}