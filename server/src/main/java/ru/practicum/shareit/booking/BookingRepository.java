package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerId(Long bookerId, Pageable pageable);

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime nowStart, LocalDateTime nowEnd, Pageable pageable);

    List<Booking> findAllByBookerIdAndEndBefore(Long bookerId, LocalDateTime now, Pageable pageable);

    List<Booking> findAllByBookerIdAndStartAfter(Long bookerId, LocalDateTime now, Pageable pageable);

    List<Booking> findAllByBookerIdAndStatus(Long bookerId, Booking.BookingStatus status, Pageable pageable);

    List<Booking> findAllByItemOwnerId(Long ownerId, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime nowStart, LocalDateTime nowEnd, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime now, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime now, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndStatus(Long ownerId, Booking.BookingStatus status, Pageable pageable);

    boolean existsByBookerIdAndItemIdAndStatusAndStartBefore(Long bookerId, Long itemId, Booking.BookingStatus status, LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(Long itemId, Booking.BookingStatus status, LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(Long itemId, Booking.BookingStatus status, LocalDateTime now);

    List<Booking> findAllByItemIdInAndStatusAndStartLessThanEqualOrderByStartDesc(List<Long> itemIds, Booking.BookingStatus status, LocalDateTime now);

    List<Booking> findAllByItemIdInAndStatusAndStartAfterOrderByStartAsc(List<Long> itemIds, Booking.BookingStatus status, LocalDateTime now);
}
