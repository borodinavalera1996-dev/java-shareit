package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void testEqualsAndHashCode() {
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Дрель");

        Item item2 = new Item();
        item2.setId(1L);

        Item item3 = new Item();
        item3.setId(2L);

        // Проверка equals
        assertEquals(item1, item2, "Items с одинаковым id должны быть равны");
        assertNotEquals(item1, item3, "Items с разными id не должны быть равны");
        assertNotEquals(item1, null, "Item не должен быть равен null");
        assertEquals(item1, item1, "Item должен быть равен самому себе");
        assertNotEquals(item1, new Object(), "Item не должен быть равен объекту другого класса");

        // Проверка hashCode
        assertEquals(item1.hashCode(), item2.hashCode(), "Хэш-коды должны совпадать");
    }
}
