package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Component
public class ItemMapper {

    public ItemDto toItemDto(Item item, List<CommentDto> commentDtos, Booking lastBooking, Booking nextBooking) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getStatus());
        itemDto.setComments(commentDtos);
        if (nextBooking != null)
            itemDto.setNextBooking(new ItemDto.BookingShortDto(nextBooking.getId(), nextBooking.getBooker().getId()));
        if (lastBooking != null)
            itemDto.setLastBooking(new ItemDto.BookingShortDto(lastBooking.getId(), lastBooking.getBooker().getId()));
        return itemDto;
    }

    public ItemDto toItemDto(Item item, List<CommentDto> commentDtos) {
        return toItemDto(item, commentDtos, null, null);
    }

    public ItemDto toItemDto(Item item) {
        return toItemDto(item, null, null, null);
    }

    public Item toItem(ItemDto itemDto) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setStatus(itemDto.getAvailable());
        return item;
    }
}
