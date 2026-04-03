package com.expenseapp.alert.domain;

import com.expenseapp.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Alert entity.
 */
class AlertTest {

    private User testUser;
    private Alert alert;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "password", "John", "Doe");
        testUser.setId(1L);

        alert = new Alert(testUser, "BUDGET", "Budget limit exceeded", "Budget", 1L);
        alert.setId(1L);
    }

    @Test
    void testAlertCreationWithFullConstructor() {
        assertNotNull(alert);
        assertEquals(testUser, alert.getUser());
        assertEquals("BUDGET", alert.getType());
        assertEquals("Budget limit exceeded", alert.getMessage());
        assertEquals("Budget", alert.getRelatedEntityType());
        assertEquals(1L, alert.getRelatedEntityId());
        assertFalse(alert.getIsRead());
        assertNull(alert.getReadAt());
    }

    @Test
    void testAlertCreationWithSimpleConstructor() {
        Alert simpleAlert = new Alert(testUser, "SYSTEM", "System maintenance");
        
        assertNotNull(simpleAlert);
        assertEquals(testUser, simpleAlert.getUser());
        assertEquals("SYSTEM", simpleAlert.getType());
        assertEquals("System maintenance", simpleAlert.getMessage());
        assertNull(simpleAlert.getRelatedEntityType());
        assertNull(simpleAlert.getRelatedEntityId());
    }

    @Test
    void testDefaultConstructor() {
        Alert emptyAlert = new Alert();
        
        assertNull(emptyAlert.getId());
        assertNull(emptyAlert.getUser());
        assertNull(emptyAlert.getType());
        assertNull(emptyAlert.getMessage());
        assertNull(emptyAlert.getRelatedEntityType());
        assertNull(emptyAlert.getRelatedEntityId());
        // isRead has default value false in entity
        assertFalse(emptyAlert.getIsRead());
        assertNull(emptyAlert.getReadAt());
    }

    @Test
    void testSetters() {
        User newUser = new User("new@example.com", "password", "Jane", "Smith");
        newUser.setId(2L);

        alert.setUser(newUser);
        alert.setType("TRANSACTION");
        alert.setMessage("New transaction alert");
        alert.setRelatedEntityType("Transaction");
        alert.setRelatedEntityId(999L);
        alert.setIsRead(true);
        alert.setReadAt(LocalDateTime.now());

        assertEquals(newUser, alert.getUser());
        assertEquals("TRANSACTION", alert.getType());
        assertEquals("New transaction alert", alert.getMessage());
        assertEquals("Transaction", alert.getRelatedEntityType());
        assertEquals(999L, alert.getRelatedEntityId());
        assertTrue(alert.getIsRead());
        assertNotNull(alert.getReadAt());
    }

    @Test
    void testIsReadWhenTrue() {
        alert.setIsRead(true);
        assertTrue(alert.isRead());
    }

    @Test
    void testIsReadWhenFalse() {
        alert.setIsRead(false);
        assertFalse(alert.isRead());
    }

    @Test
    void testIsReadWhenNull() {
        alert.setIsRead(null);
        assertFalse(alert.isRead());
    }

    @Test
    void testMarkAsRead() {
        assertFalse(alert.isRead());
        assertNull(alert.getReadAt());

        alert.markAsRead();

        assertTrue(alert.isRead());
        assertNotNull(alert.getReadAt());
    }

    @Test
    void testMarkAsUnread() {
        alert.setIsRead(true);
        alert.setReadAt(LocalDateTime.now());

        assertTrue(alert.isRead());
        assertNotNull(alert.getReadAt());

        alert.markAsUnread();

        assertFalse(alert.isRead());
        assertNull(alert.getReadAt());
    }

    @Test
    void testSetIsReadSetsReadAtAutomatically() {
        alert.setIsRead(true);
        assertTrue(alert.isRead());
        assertNotNull(alert.getReadAt());
    }

    @Test
    void testSetIsReadDoesNotClearReadAtWhenFalse() {
        LocalDateTime readAt = LocalDateTime.now();
        alert.setReadAt(readAt);
        alert.setIsRead(false);
        
        assertFalse(alert.isRead());
        // Note: The setter doesn't clear readAt, only markAsUnread does
        assertEquals(readAt, alert.getReadAt());
    }

    @Test
    void testIdSetter() {
        alert.setId(999L);
        assertEquals(999L, alert.getId());
    }

    @Test
    void testAlertWithNullType() {
        alert.setType(null);
        assertNull(alert.getType());
    }

    @Test
    void testAlertWithNullMessage() {
        alert.setMessage(null);
        assertNull(alert.getMessage());
    }

    @Test
    void testAlertWithNullRelatedEntityType() {
        alert.setRelatedEntityType(null);
        assertNull(alert.getRelatedEntityType());
    }

    @Test
    void testAlertWithNullRelatedEntityId() {
        alert.setRelatedEntityId(null);
        assertNull(alert.getRelatedEntityId());
    }
}