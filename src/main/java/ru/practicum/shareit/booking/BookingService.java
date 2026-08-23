package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    private final ItemRepository itemRepository;

    public BookingDto create(@Valid BookingInputDto bookingDto, Long userId) {
        User user = getUser(userId);
        Item item = getItem(bookingDto.getItemId());
        if (!item.getStatus())
            throw new NotAvailableException("Предмет не доступен");
        Booking booking = bookingMapper.toBooking(bookingDto, item, user);
        booking.setStatus(Booking.BookingStatus.WAITING);
        booking.setBooker(user);
        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toBookingDto(saved);
    }

    public BookingDto approved(Long bookingId, Long userId, boolean approved) {
        try {
            getUser(userId);
        } catch (NotFoundException ex) {
            throw new NotAvailableException(ex.getMessage());
        }
        Booking booking = getBooking(bookingId);
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ConflictException("Пользователь с id " + userId + " не является владельцем вещи");
        }
        if (approved)
            booking.setStatus(Booking.BookingStatus.APPROVED);
        else
            booking.setStatus(Booking.BookingStatus.REJECTED);
        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toBookingDto(saved);
    }

    public BookingDto get(Long bookingId, Long userId) {
        getUser(userId);
        Booking booking = getBooking(bookingId);
        if (booking.getBooker().getId().equals(userId) || booking.getItem().getOwner().getId().equals(userId)) {
            return bookingMapper.toBookingDto(booking);
        }
        throw new ConflictException("Бронирование не принадлежит пользователю с id " + userId);
    }

    public List<BookingDto> getAllByUserIdSortedByDate(Long userId, String stateStr) {
        getUser(userId);
        BookingStatus state = BookingStatus.from(stateStr)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateStr));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case BookingStatus.ALL -> bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
            case BookingStatus.CURRENT ->
                    bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
            case BookingStatus.PAST -> bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case BookingStatus.FUTURE -> bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            case BookingStatus.WAITING ->
                    bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, Booking.BookingStatus.WAITING);
            case BookingStatus.REJECTED ->
                    bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, Booking.BookingStatus.REJECTED);
            default -> List.of();
        };

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    public List<BookingDto> getAllByOwnerSortedByDate(Long userId, String stateStr) {
        getUser(userId);
        BookingStatus state = BookingStatus.from(stateStr)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateStr));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case BookingStatus.ALL -> bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId);
            case BookingStatus.CURRENT ->
                    bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
            case BookingStatus.PAST -> bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now);
            case BookingStatus.FUTURE ->
                    bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now);
            case BookingStatus.WAITING ->
                    bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Booking.BookingStatus.WAITING);
            case BookingStatus.REJECTED ->
                    bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Booking.BookingStatus.REJECTED);
            default -> List.of();
        };

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    private Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено"));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private Item getItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + id + " не найден"));
    }
}
