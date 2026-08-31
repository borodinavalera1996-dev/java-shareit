package ru.practicum.shareit.booking.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

@Component
public class BookingMapper {
    public BookingDto toBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingDto.ItemDto itemDto = new BookingDto.ItemDto(
                booking.getItem().getId(),
                booking.getItem().getName()
        );
        BookingDto.UserDto bookerDto = new BookingDto.UserDto(
                booking.getBooker().getId(),
                booking.getBooker().getName()
        );
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(itemDto)
                .booker(bookerDto)
                .status(BookingDto.BookingStatus.valueOf(booking.getStatus().name()))
                .build();
    }

    public Booking toBooking(BookingInputDto bookingDto, Item item, User booker) {
        if (bookingDto == null) {
            return null;
        }

        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Booking.BookingStatus.WAITING);

        return booking;
    }
}
