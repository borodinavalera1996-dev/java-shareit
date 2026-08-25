package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId, LocalDateTime nowStart, LocalDateTime nowEnd);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime now);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime now);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, Booking.BookingStatus status);

    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long ownerId, LocalDateTime nowStart, LocalDateTime nowEnd);

    List<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime now);

    List<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime now);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, Booking.BookingStatus status);

    boolean existsByBookerIdAndItemIdAndStatusAndStartBefore(Long bookerId, Long itemId, Booking.BookingStatus status, LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(Long itemId, Booking.BookingStatus status, LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(Long itemId, Booking.BookingStatus status, LocalDateTime now);

    List<Booking> findAllByItemIdInAndStatusAndStartLessThanEqualOrderByStartDesc(List<Long> itemIds, Booking.BookingStatus status, LocalDateTime now);

    List<Booking> findAllByItemIdInAndStatusAndStartAfterOrderByStartAsc(List<Long> itemIds, Booking.BookingStatus status, LocalDateTime now);
}
