package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testGettersAndSetters() {
        User user = new User();
        user.setId(1L);
        user.setName("Иван");
        user.setEmail("ivan@mail.com");

        assertEquals(1L, user.getId());
        assertEquals("Иван", user.getName());
        assertEquals("ivan@mail.com", user.getEmail());
    }

    @Test
    void testToString() {
        User user = new User();
        user.setId(1L);
        user.setName("Иван");
        user.setEmail("ivan@mail.com");

        String toStringResult = user.toString();

        assertTrue(toStringResult.contains("User"));
        assertTrue(toStringResult.contains("id=1"));
        assertTrue(toStringResult.contains("name=Иван"));
        assertTrue(toStringResult.contains("email=ivan@mail.com"));
    }

    @Test
    void testEquals_SameObject_ReturnsTrue() {
        User user = new User();
        user.setId(1L);

        assertTrue(user.equals(user));
    }

    @Test
    void testEquals_DifferentClassOrNull_ReturnsFalse() {
        User user = new User();
        user.setId(1L);

        assertFalse(user.equals(null));
        assertFalse(user.equals("Not a User object"));
    }

    @Test
    void testEquals_WithSameId_ReturnsTrue() {
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(1L);

        assertTrue(user1.equals(user2));
    }

    @Test
    void testEquals_WithDifferentId_ReturnsFalse() {
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(2L);

        assertFalse(user1.equals(user2));
    }

    @Test
    void testEquals_WithNullId_ReturnsFalse() {
        User user1 = new User(); // id == null
        User user2 = new User();
        user2.setId(1L);

        assertFalse(user1.equals(user2));
    }

    @Test
    void testHashCode() {
        User user1 = new User();
        User user2 = new User();

        assertEquals(user1.hashCode(), user2.hashCode());
    }
}
