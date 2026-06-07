package ru.practicum.shareit.request;

import lombok.Data;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Data
public class ItemRequest {
    Integer id;
    String description;
    User requestor;
    LocalDateTime created;
}
