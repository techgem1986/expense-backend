package com.expenseapp.user.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity.
 */
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("test@example.com", "hashedPassword", "John", "Doe");
        user.setId(1L);
    }

    @Test
    void testUserCreationWithConstructor() {
        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashedPassword", user.getPasswordHash());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertTrue(user.getIsActive());
    }

    @Test
    void testDefaultConstructor() {
        User emptyUser = new User();
        
        assertNull(emptyUser.getId());
        assertNull(emptyUser.getEmail());
        assertNull(emptyUser.getPasswordHash());
        assertNull(emptyUser.getFirstName());
        assertNull(emptyUser.getLastName());
        // isActive has default value in entity
        assertTrue(emptyUser.getIsActive());
    }

    @Test
    void testSetters() {
        user.setEmail("new@example.com");
        user.setPasswordHash("newHashedPassword");
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setIsActive(false);

        assertEquals("new@example.com", user.getEmail());
        assertEquals("newHashedPassword", user.getPasswordHash());
        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertFalse(user.getIsActive());
    }

    @Test
    void testGetFullName() {
        assertEquals("John Doe", user.getFullName());
    }

    @Test
    void testGetFullNameWithEmptyNames() {
        user.setFirstName("");
        user.setLastName("");
        assertEquals(" ", user.getFullName());
    }

    @Test
    void testIsActiveWhenTrue() {
        user.setIsActive(true);
        assertTrue(user.isActive());
    }

    @Test
    void testIsActiveWhenFalse() {
        user.setIsActive(false);
        assertFalse(user.isActive());
    }

    @Test
    void testIsActiveWhenNull() {
        user.setIsActive(null);
        assertFalse(user.isActive());
    }

    @Test
    void testIdSetter() {
        user.setId(999L);
        assertEquals(999L, user.getId());
    }

    @Test
    void testUserWithNullEmail() {
        user.setEmail(null);
        assertNull(user.getEmail());
    }

    @Test
    void testUserWithNullPassword() {
        user.setPasswordHash(null);
        assertNull(user.getPasswordHash());
    }

    @Test
    void testUserWithNullFirstName() {
        user.setFirstName(null);
        assertNull(user.getFirstName());
    }

    @Test
    void testUserWithNullLastName() {
        user.setLastName(null);
        assertNull(user.getLastName());
    }
}