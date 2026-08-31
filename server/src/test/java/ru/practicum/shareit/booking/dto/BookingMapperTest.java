package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

    private BookingMapper bookingMapper;
    private Booking booking;
    private BookingInputDto bookingInputDto;
    private Item item;
    private User booker;

    @BeforeEach
    void setUp() {
        bookingMapper = new BookingMapper();

        booker = new User();
        booker.setId(10L);
        booker.setName("Алексей");

        item = new Item();
        item.setId(5L);
        item.setName("Перфоратор");

        booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Booking.BookingStatus.APPROVED);

        bookingInputDto = new BookingInputDto();
        bookingInputDto.setItemId(5L);
        bookingInputDto.setStart(LocalDateTime.now().plusDays(3));
        bookingInputDto.setEnd(LocalDateTime.now().plusDays(4));
    }

    @Test
    void toBookingDto_whenValidBooking_thenReturnCorrectDto() {
        BookingDto dto = bookingMapper.toBookingDto(booking);

        assertNotNull(dto);
        assertEquals(booking.getId(), dto.getId());
        assertEquals(booking.getStart(), dto.getStart());
        assertEquals(booking.getEnd(), dto.getEnd());

        assertNotNull(dto.getItem());
        assertEquals(item.getId(), dto.getItem().getId());
        assertEquals(item.getName(), dto.getItem().getName());

        assertNotNull(dto.getBooker());
        assertEquals(booker.getId(), dto.getBooker().getId());
        assertEquals(booker.getName(), dto.getBooker().getName());

        assertEquals(BookingDto.BookingStatus.APPROVED, dto.getStatus());
    }

    @Test
    void toBookingDto_whenBookingIsNull_thenReturnNull() {
        assertNull(bookingMapper.toBookingDto(null));
    }

    @Test
    void toBooking_whenValidArgs_thenReturnCorrectEntity() {
        Booking entity = bookingMapper.toBooking(bookingInputDto, item, booker);

        assertNotNull(entity);
        assertNull(entity.getId());
        assertEquals(bookingInputDto.getStart(), entity.getStart());
        assertEquals(bookingInputDto.getEnd(), entity.getEnd());
        assertEquals(item, entity.getItem());
        assertEquals(booker, entity.getBooker());
        assertEquals(Booking.BookingStatus.WAITING, entity.getStatus());
    }

    @Test
    void toBooking_whenDtoIsNull_thenReturnNull() {
        assertNull(bookingMapper.toBooking(null, item, booker));
    }
}
