package ru.practicum.shareit.request.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Data
public class ItemRequestDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<AnswerDto> items;

    @Data
    public static class AnswerDto {
        private final Long itemId;
        private final String name;
        private final Long ownerId;
    }
}
