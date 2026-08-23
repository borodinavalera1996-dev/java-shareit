package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class ItemDto {
    Long id;
    @NotBlank
    String name;
    @NotBlank
    String description;
    @NotNull
    Boolean available;
    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;
    List<CommentDto> comments;

    @Data
    @AllArgsConstructor
    public static class BookingShortDto {
        private Long id;
        private Long bookerId;
    }
}
