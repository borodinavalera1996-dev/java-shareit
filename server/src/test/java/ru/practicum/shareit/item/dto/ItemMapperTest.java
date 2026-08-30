package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    private ItemMapper itemMapper;
    private Item item;
    private ItemDto itemDto;
    private User booker;
    private Booking lastBooking;
    private Booking nextBooking;
    private List<CommentDto> comments;

    @BeforeEach
    void setUp() {
        itemMapper = new ItemMapper();

        item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Ударная");
        item.setStatus(true);

        itemDto = new ItemDto();
        itemDto.setId(2L);
        itemDto.setName("Отвертка");
        itemDto.setDescription("Шлицевая");
        itemDto.setAvailable(false);

        booker = new User();
        booker.setId(10L);

        lastBooking = new Booking();
        lastBooking.setId(100L);
        lastBooking.setBooker(booker);

        nextBooking = new Booking();
        nextBooking.setId(200L);
        nextBooking.setBooker(booker);

        comments = List.of(new CommentDto());
    }

    @Test
    void toItemDto_withAllArgs_whenRequestAndBookingsNotNull_thenReturnFullDto() {
        ItemRequest request = new ItemRequest();
        request.setId(5L);
        item.setRequest(request);

        ItemDto result = itemMapper.toItemDto(item, comments, lastBooking, nextBooking);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getStatus(), result.getAvailable());
        assertEquals(comments, result.getComments());
        assertEquals(5L, result.getRequestId());

        assertNotNull(result.getLastBooking());
        assertEquals(100L, result.getLastBooking().getId());
        assertEquals(10L, result.getLastBooking().getBookerId());

        assertNotNull(result.getNextBooking());
        assertEquals(200L, result.getNextBooking().getId());
        assertEquals(10L, result.getNextBooking().getBookerId());
    }

    @Test
    void toItemDto_withAllArgs_whenRequestAndBookingsAreNull_thenFieldsAreNull() {
        item.setRequest(null);

        ItemDto result = itemMapper.toItemDto(item, Collections.emptyList(), null, null);

        assertNotNull(result);
        assertNull(result.getRequestId());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertTrue(result.getComments().isEmpty());
    }

    @Test
    void toItemDto_withTwoArgs_thenReturnDtoWithNullBookings() {
        ItemDto result = itemMapper.toItemDto(item, comments);

        assertNotNull(result);
        assertEquals(comments, result.getComments());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }

    @Test
    void toItemDto_withOneArg_thenReturnDtoWithNullCommentsAndBookings() {
        ItemDto result = itemMapper.toItemDto(item);

        assertNotNull(result);
        assertNull(result.getComments());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }

    @Test
    void toItem_whenValidDto_thenReturnCorrectEntity() {
        Item result = itemMapper.toItem(itemDto);

        assertNotNull(result);
        assertEquals(itemDto.getId(), result.getId());
        assertEquals(itemDto.getName(), result.getName());
        assertEquals(itemDto.getDescription(), result.getDescription());
        assertEquals(itemDto.getAvailable(), result.getStatus());
        assertNull(result.getOwner());
        assertNull(result.getRequest());
    }
}
