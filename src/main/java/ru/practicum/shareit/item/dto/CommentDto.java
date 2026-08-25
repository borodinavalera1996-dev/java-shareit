package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;
    private Long itemId;
    @NotBlank
    private String text;
    private String authorName;
    private LocalDateTime created;
}
