package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class ItemDto {
    Long id;
    String name;
    String description;
    Boolean available;
    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;
    List<CommentDto> comments;
    private Long requestId;

    @Data
    @AllArgsConstructor
    public static class BookingShortDto {
        private Long id;
        private Long bookerId;
    }
}
