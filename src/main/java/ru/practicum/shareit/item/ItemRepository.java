package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {

    Item createItem(Item item);

    Item updateItem(Item item);

    Optional<Item> getItemById(Long id);

    List<Item> searchItems(String text);

    List<Item> getAllItemByUser(Long userId);
}
