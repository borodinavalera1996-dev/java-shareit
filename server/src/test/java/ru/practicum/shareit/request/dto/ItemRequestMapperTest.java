package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestMapperTest {

    private ItemRequestMapper itemRequestMapper;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;
    private Item item;
    private User owner;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        itemRequestMapper = new ItemRequestMapper();
        now = LocalDateTime.now();

        owner = new User();
        owner.setId(2L);

        item = new Item();
        item.setId(3L);
        item.setName("Дрель");
        item.setOwner(owner);

        itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Нужна дрель");
        itemRequest.setCreated(now);

        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("Нужна дрель");
    }

    @Test
    void toItemRequest_whenValidDto_thenReturnCorrectEntity() {
        ItemRequest result = itemRequestMapper.toItemRequest(itemRequestDto);

        assertNotNull(result);
        assertEquals(itemRequestDto.getDescription(), result.getDescription());
    }

    @Test
    void toAnswerDto_whenValidItem_thenReturnCorrectAnswerDto() {
        ItemRequestDto.AnswerDto result = itemRequestMapper.toAnswerDto(item);

        assertNotNull(result);
        assertEquals(item.getName(), result.getName());
    }

    @Test
    void toItemRequestDto_withOneArg_thenReturnDtoWithNullItems() {
        ItemRequestDto result = itemRequestMapper.toItemRequestDto(itemRequest);

        assertNotNull(result);
        assertEquals(itemRequest.getId(), result.getId());
        assertEquals(itemRequest.getDescription(), result.getDescription());
        assertEquals(itemRequest.getCreated(), result.getCreated());
        assertNull(result.getItems());
    }

    @Test
    void toItemRequestDto_withTwoArgs_whenItemsNotNull_thenReturnDtoWithItems() {
        ItemRequestDto result = itemRequestMapper.toItemRequestDto(itemRequest, List.of(item));

        assertNotNull(result);
        assertEquals(itemRequest.getId(), result.getId());
        assertEquals(itemRequest.getDescription(), result.getDescription());
        assertEquals(itemRequest.getCreated(), result.getCreated());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void toItemRequestDto_withTwoArgs_whenItemsNull_thenReturnDtoWithNullItems() {
        ItemRequestDto result = itemRequestMapper.toItemRequestDto(itemRequest, null);

        assertNotNull(result);
        assertNull(result.getItems());
    }
}
