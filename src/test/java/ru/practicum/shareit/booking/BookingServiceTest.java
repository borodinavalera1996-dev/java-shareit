package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

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
        booker.setName("Booker");

        owner = new User();
        owner.setId(2L);
        owner.setName("Owner");

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
        bookingInputDto.setStart(booking.getStart());
        bookingInputDto.setEnd(booking.getEnd());

        bookingDto = BookingDto.builder()
                .id(1L)
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }

    @Test
    void create_whenValid_thenSaveAndReturnDto() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(bookingMapper.toBooking(any(), any(), any())).thenReturn(booking);
        Mockito.when(bookingRepository.save(any())).thenReturn(booking);
        Mockito.when(bookingMapper.toBookingDto(any())).thenReturn(bookingDto);

        BookingDto result = bookingService.create(bookingInputDto, 1L);

        assertNotNull(result);
        assertEquals(bookingDto.getId(), result.getId());
        Mockito.verify(bookingRepository, Mockito.times(1)).save(any());
    }

    @Test
    void create_whenItemNotAvailable_thenThrowNotAvailableException() {
        item.setStatus(false);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotAvailableException.class, () -> bookingService.create(bookingInputDto, 1L));
        Mockito.verify(bookingRepository, Mockito.never()).save(any());
    }

    @Test
    void approved_whenApprovedTrue_thenSetApprovedStatus() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.save(any())).thenReturn(booking);
        Mockito.when(bookingMapper.toBookingDto(any())).thenReturn(bookingDto);

        BookingDto result = bookingService.approved(1L, 2L, true);

        assertNotNull(result);
        assertEquals(Booking.BookingStatus.APPROVED, booking.getStatus());
        Mockito.verify(bookingRepository, Mockito.times(1)).save(booking);
    }

    @Test
    void approved_whenApprovedFalse_thenSetRejectedStatus() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.save(any())).thenReturn(booking);
        Mockito.when(bookingMapper.toBookingDto(any())).thenReturn(bookingDto);

        BookingDto result = bookingService.approved(1L, 2L, false);

        assertNotNull(result);
        assertEquals(Booking.BookingStatus.REJECTED, booking.getStatus());
    }

    @Test
    void approved_whenUserNotOwner_thenThrowConflictException() {
        Mockito.when(userRepository.findById(3L)).thenReturn(Optional.of(new User()));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ConflictException.class, () -> bookingService.approved(1L, 3L, true));
        Mockito.verify(bookingRepository, Mockito.never()).save(any());
    }

    @Test
    void get_whenUserIsBookerOrOwner_thenReturnDto() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        Mockito.when(bookingMapper.toBookingDto(any())).thenReturn(bookingDto);

        BookingDto result = bookingService.get(1L, 1L);

        assertNotNull(result);
        Mockito.verify(bookingMapper, Mockito.times(1)).toBookingDto(booking);
    }

    @Test
    void get_whenUserNotBookerOrOwner_thenThrowConflictException() {
        Mockito.when(userRepository.findById(3L)).thenReturn(Optional.of(new User()));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ConflictException.class, () -> bookingService.get(1L, 3L));
    }

    @Test
    void getAllByUserIdSortedByDate_whenStateAll_thenCallFindAllByBookerId() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        Mockito.when(bookingRepository.findAllByBookerIdOrderByStartDesc(1L)).thenReturn(List.of(booking));
        Mockito.when(bookingMapper.toBookingDto(any())).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getAllByUserIdSortedByDate(1L, "ALL");

        assertNotNull(result);
        assertEquals(1, result.size());
        Mockito.verify(bookingRepository, Mockito.times(1)).findAllByBookerIdOrderByStartDesc(1L);
    }

    @Test
    void getAllByUserIdSortedByDate_whenStateUnknown_thenThrowIllegalArgumentException() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(booker));

        assertThrows(IllegalArgumentException.class, () -> bookingService.getAllByUserIdSortedByDate(1L, "INVALID"));
    }

    @Test
    void getAllByOwnerSortedByDate_whenStateAll_thenCallFindAllByItemOwnerId() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(2L)).thenReturn(List.of(booking));
        Mockito.when(bookingMapper.toBookingDto(any())).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getAllByOwnerSortedByDate(2L, "ALL");

        assertNotNull(result);
        assertEquals(1, result.size());
        Mockito.verify(bookingRepository, Mockito.times(1)).findAllByItemOwnerIdOrderByStartDesc(2L);
    }
}
