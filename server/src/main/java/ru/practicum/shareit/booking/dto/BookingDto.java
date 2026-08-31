package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDto {
    Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private ItemDto item;
    private UserDto booker;
    private BookingStatus status;

    @Data
    public static class ItemDto {
        private final Long id;
        private final String name;
    }

    @Data
    public static class UserDto {
        private final Long id;
        private final String name;
    }

    public enum BookingStatus {
        WAITING,
        APPROVED,
        REJECTED,
        CANCELED;
    }
}