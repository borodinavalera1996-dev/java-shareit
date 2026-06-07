package ru.practicum.shareit.item.model;

import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

@Data
public class Item {
    Long id;
    String name;
    String description;
    Boolean status;
    User owner;
    ItemRequest request;
}
