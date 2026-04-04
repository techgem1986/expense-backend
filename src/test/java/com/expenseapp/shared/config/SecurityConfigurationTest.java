package com.expenseapp.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void securityConfigurationLoads() {
        // Verify the security configuration bean is loaded
        assertTrue(applicationContext.containsBean("securityFilterChain"));
    }

    @Test
    void securityFilterChainIsConfigured() {
        SecurityFilterChain filterChain = applicationContext.getBean(SecurityFilterChain.class);
        assertNotNull(filterChain);
    }
}