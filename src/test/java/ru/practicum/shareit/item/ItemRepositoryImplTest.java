package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ItemRepositoryImplTest {

    private ItemRepositoryImpl itemRepository;
    private User owner;
    private Item item;

    @BeforeEach
    void setUp() {
        itemRepository = new ItemRepositoryImpl();

        owner = new User();
        owner.setId(1L);
        owner.setName("Ivan");
        owner.setEmail("ivan@mail.com");

        item = new Item();
        item.setName("Отвертка");
        item.setDescription("Аккумуляторная отвертка");
        item.setStatus(true);
        item.setOwner(owner);
    }

    @Test
    void createItem_whenValid_thenSuccessAndGeneratesId() {
        Item savedItem = itemRepository.createItem(item);

        assertNotNull(savedItem);
        assertNotNull(savedItem.getId());
        assertEquals(1L, savedItem.getId());
        assertEquals("Отвертка", savedItem.getName());
    }

    @Test
    void updateItem_whenItemExists_thenUpdatesInMap() {
        Item savedItem = itemRepository.createItem(item);
        savedItem.setName("Новая отвертка");
        savedItem.setStatus(false);

        Item updatedItem = itemRepository.updateItem(savedItem);

        assertEquals("Новая отвертка", updatedItem.getName());
        assertFalse(updatedItem.getStatus());

        Optional<Item> foundItem = itemRepository.getItemById(savedItem.getId());
        assertTrue(foundItem.isPresent());
        assertEquals("Новая отвертка", foundItem.get().getName());
    }

    @Test
    void getItemById_whenItemExists_thenReturnsOptionalWithItem() {
        Item savedItem = itemRepository.createItem(item);

        Optional<Item> foundItemOpt = itemRepository.getItemById(savedItem.getId());

        assertTrue(foundItemOpt.isPresent());
        assertEquals(savedItem.getId(), foundItemOpt.get().getId());
    }

    @Test
    void getItemById_whenItemDoesNotExist_thenReturnsEmptyOptional() {
        Optional<Item> foundItemOpt = itemRepository.getItemById(99L);

        assertTrue(foundItemOpt.isEmpty());
    }

    @Test
    void searchItems_whenMatchNameOrDescriptionCaseInsensitive_thenReturnsList() {
        itemRepository.createItem(item);

        List<Item> resultByName = itemRepository.searchItems("ОТВЕ");
        List<Item> resultByDesc = itemRepository.searchItems("аккум");

        assertEquals(1, resultByName.size());
        assertEquals("Отвертка", resultByName.get(0).getName());
        assertEquals(1, resultByDesc.size());
    }

    @Test
    void searchItems_whenItemIsNotAvailable_thenReturnsEmptyList() {
        item.setStatus(false);
        itemRepository.createItem(item);

        List<Item> result = itemRepository.searchItems("Отвертка");

        assertTrue(result.isEmpty());
    }

    @Test
    void searchItems_whenNoMatch_thenReturnsEmptyList() {
        itemRepository.createItem(item);

        List<Item> result = itemRepository.searchItems("Дрель");

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllItemByUser_whenUserHasItems_thenReturnsCorrectList() {
        itemRepository.createItem(item);

        Item secondItem = new Item();
        secondItem.setName("Дрель");
        secondItem.setStatus(true);
        secondItem.setOwner(owner);
        itemRepository.createItem(secondItem);

        User anotherUser = new User();
        anotherUser.setId(2L);
        Item thirdItem = new Item();
        thirdItem.setName("Лобзик");
        thirdItem.setStatus(true);
        thirdItem.setOwner(anotherUser);
        itemRepository.createItem(thirdItem);

        List<Item> ownerItems = itemRepository.getAllItemByUser(1L);
        List<Item> otherUserItems = itemRepository.getAllItemByUser(2L);

        assertEquals(2, ownerItems.size());
        assertEquals(1, otherUserItems.size());
        assertEquals("Лобзик", otherUserItems.get(0).getName());
    }
}
