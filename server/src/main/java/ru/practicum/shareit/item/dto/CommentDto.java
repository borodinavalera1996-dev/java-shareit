package ru.practicum.shareit.item.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;
    private Long itemId;
    private String text;
    private String authorName;
    private LocalDateTime created;
}
