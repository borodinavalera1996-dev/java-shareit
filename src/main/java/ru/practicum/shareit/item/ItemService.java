package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    public ItemDto createItem(@Valid ItemDto itemDto, Long userId) {
        log.info("Создание вещи: {}", itemDto);
        User userById = getUser(userId);
        Item item = itemMapper.toItem(itemDto);
        item.setOwner(userById);
        item = itemRepository.createItem(item);
        return itemMapper.toItemDto(item);
    }

    public ItemDto updateItem(@Valid UpdateItemDto itemDto, Long id, Long userId) {
        log.info("Обновление вещи с id {}: {}", id, itemDto);

        getUser(userId);
        Item targetItem = getItem(id);

        if (!targetItem.getOwner().getId().equals(userId)) {
            log.warn("Пользователь {} не является владельцем вещи {}", userId, id);
            throw new NotFoundException("Вещь не принадлежит данному пользователю");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            targetItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            targetItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            targetItem.setStatus(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.updateItem(targetItem);
        return itemMapper.toItemDto(updatedItem);
    }

    private Item getItem(Long id) {
        return itemRepository.getItemById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + id + " не найдена"));
    }

    private User getUser(Long userId) {
        return userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    public ItemDto getItemById(Long id) {
        log.info("Получение вещи по id: {}", id);
        Item item = getItem(id);
        return itemMapper.toItemDto(item);
    }

    public List<ItemDto> searchItems(String text) {
        log.info("Поиск вещей по запросу: {}", text);
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.searchItems(text).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public List<ItemDto> getAllItemByUser(Long userId) {
        log.info("Получение всех вещей пользователя с id: {}", userId);
        getUser(userId);
        return itemRepository.getAllItemByUser(userId).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
