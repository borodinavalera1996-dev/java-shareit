package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    public BookingDto create(BookingInputDto bookingDto, Long userId) {
        User user = getUser(userId);
        Item item = getItem(bookingDto.getItemId());
        if (!item.getStatus())
            throw new NotAvailableException("Предмет не доступен");
        Booking booking = bookingMapper.toBooking(bookingDto, item, user);
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
        if (!booking.getStatus().equals(Booking.BookingStatus.WAITING)) {
            throw new ConflictException("Статус бронирования не соответствует ожидаемому (WAITING)");
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

    public List<BookingDto> getAllByUserIdSortedByDate(Long userId, String stateStr,
                                                       Integer from, Integer size) {
        getUser(userId);
        BookingStatus state = BookingStatus.from(stateStr)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateStr));

        LocalDateTime now = LocalDateTime.now();
        PageRequest request = PageRequest.of(from / size, size, Sort.by("start").descending());
        List<Booking> bookings = switch (state) {
            case BookingStatus.ALL -> bookingRepository.findAllByBookerId(userId, request);
            case BookingStatus.CURRENT ->
                    bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfter(userId, now, now, request);
            case BookingStatus.PAST -> bookingRepository.findAllByBookerIdAndEndBefore(userId, now, request);
            case BookingStatus.FUTURE -> bookingRepository.findAllByBookerIdAndStartAfter(userId, now, request);
            case BookingStatus.WAITING ->
                    bookingRepository.findAllByBookerIdAndStatus(userId, Booking.BookingStatus.WAITING, request);
            case BookingStatus.REJECTED ->
                    bookingRepository.findAllByBookerIdAndStatus(userId, Booking.BookingStatus.REJECTED, request);
        };

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    public List<BookingDto> getAllByOwnerSortedByDate(Long userId, String stateStr,
                                                      Integer from, Integer size) {
        getUser(userId);
        BookingStatus state = BookingStatus.from(stateStr)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateStr));

        PageRequest request = PageRequest.of(from / size, size, Sort.by("start").descending());
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case BookingStatus.ALL -> bookingRepository.findAllByItemOwnerId(userId, request);
            case BookingStatus.CURRENT ->
                    bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfter(userId, now, now, request);
            case BookingStatus.PAST -> bookingRepository.findAllByItemOwnerIdAndEndBefore(userId, now, request);
            case BookingStatus.FUTURE -> bookingRepository.findAllByItemOwnerIdAndStartAfter(userId, now, request);
            case BookingStatus.WAITING ->
                    bookingRepository.findAllByItemOwnerIdAndStatus(userId, Booking.BookingStatus.WAITING, request);
            case BookingStatus.REJECTED ->
                    bookingRepository.findAllByItemOwnerIdAndStatus(userId, Booking.BookingStatus.REJECTED, request);
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
