package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingService bookingService;

    private User booker;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingInputDto bookingInputDto;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        booker = new User();
        booker.setId(1L);
        booker.setName("Алексей");

        owner = new User();
        owner.setId(2L);
        owner.setName("Иван");

        item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setStatus(true);
        item.setOwner(owner);

        booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Booking.BookingStatus.WAITING);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        bookingInputDto = new BookingInputDto();
        bookingInputDto.setItemId(1L);
        bookingInputDto.setStart(LocalDateTime.now().plusDays(1));
        bookingInputDto.setEnd(LocalDateTime.now().plusDays(2));

        bookingDto = new BookingDto();
        bookingDto.setId(1L);
    }

    @Test
    void create_whenDatesInvalid_thenThrowValidationException() {
        bookingInputDto.setStart(LocalDateTime.now().plusDays(5));
        bookingInputDto.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(ValidationException.class, () -> bookingService.create(bookingInputDto, 1L));
        Mockito.verify(bookingRepository, Mockito.never()).save(any());
    }

    @Test
    void create_whenItemNotAvailable_thenThrowNotAvailableException() {
        item.setStatus(false);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotAvailableException.class, () -> bookingService.create(bookingInputDto, 1L));
    }

    @Test
    void create_whenValid_thenSaveAndReturnDto() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(bookingMapper.toBooking(any(), any(), any())).thenReturn(booking);
        Mockito.when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        Mockito.when(bookingMapper.toBookingDto(any(Booking.class))).thenReturn(bookingDto);

        BookingDto result = bookingService.create(bookingInputDto, 1L);

        assertNotNull(result);
        assertEquals(bookingDto.getId(), result.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).save(any(Booking.class));
    }

    @Test
    void approved_whenUserNotOwner_thenThrowConflictException() {
        Mockito.when(userRepository.findById(3L)).thenReturn(Optional.of(new User()));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ConflictException.class, () -> bookingService.approved(1L, 3L, true));
    }

    @Test
    void approved_whenStatusNotWaiting_thenThrowConflictException() {
        booking.setStatus(Booking.BookingStatus.APPROVED);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ConflictException.class, () -> bookingService.approved(1L, 2L, true));
    }

    @Test
    void approved_whenTrue_thenSetApprovedAndSave() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        Mockito.when(bookingMapper.toBookingDto(any(Booking.class))).thenReturn(bookingDto);

        bookingService.approved(1L, 2L, true);

        assertEquals(Booking.BookingStatus.APPROVED, booking.getStatus());
        Mockito.verify(bookingRepository, Mockito.times(1)).save(booking);
    }

    @Test
    void get_whenUserNotRelated_thenThrowConflictException() {
        Mockito.when(userRepository.findById(3L)).thenReturn(Optional.of(new User()));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ConflictException.class, () -> bookingService.get(1L, 3L));
    }

    @Test
    void getAllByUserIdSortedByDate_withUnknownState_thenThrowIllegalArgumentException() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(booker));

        assertThrows(IllegalArgumentException.class, () ->
                bookingService.getAllByUserIdSortedByDate(1L, "INVALID_STATE", 0, 10)
        );
    }

    @Test
    void getAllByUserIdSortedByDate_withStateAll_thenReturnList() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        Mockito.when(bookingRepository.findAllByBookerId(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(booking));
        Mockito.when(bookingMapper.toBookingDto(any(Booking.class))).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getAllByUserIdSortedByDate(1L, "ALL", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        Mockito.verify(bookingRepository, Mockito.times(1)).findAllByBookerId(eq(1L), any(Pageable.class));
    }

    @Test
    void getAllByOwnerSortedByDate_withStateFuture_thenReturnList() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findAllByItemOwnerIdAndStartAfter(eq(2L), any(), any(Pageable.class)))
                .thenReturn(List.of(booking));
        Mockito.when(bookingMapper.toBookingDto(any(Booking.class))).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getAllByOwnerSortedByDate(2L, "FUTURE", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemOwnerIdAndStartAfter(eq(2L), any(), any(Pageable.class));
    }
}
