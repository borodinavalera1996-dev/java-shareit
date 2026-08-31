package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    private final ItemRequestRepository itemRequestRepository;

    public ItemDto createItem(ItemDto itemDto, Long userId) {
        log.info("Создание вещи: {}", itemDto);
        User userById = getUser(userId);
        ItemRequest request = itemDto.getRequestId() !=  null ? getRequest(itemDto.getRequestId()) : null;
        Item item = itemMapper.toItem(itemDto);
        item.setOwner(userById);
        item.setRequest(request);
        item = itemRepository.save(item);
        return itemMapper.toItemDto(item);
    }

    public ItemDto updateItem(UpdateItemDto itemDto, Long id, Long userId) {
        log.info("Обновление вещи с id {}: {}", id, itemDto);

        getUser(userId);
        Item targetItem = getItem(id);

        if (!targetItem.getOwner().getId().equals(userId)) {
            log.warn("Пользователь {} не является владельцем вещи {}", userId, id);
            throw new NotFoundException("Вещь не принадлежит данному пользователю");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            targetItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            targetItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            targetItem.setStatus(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(targetItem);
        return itemMapper.toItemDto(updatedItem);
    }

    public ItemDto getItemById(Long id, Long userId) {
        log.info("Получение вещи по id: {}", id);
        Item item = getItem(id);
        ItemDto itemDto;
        LocalDateTime now = LocalDateTime.now();
        if (item.getOwner().getId().equals(userId)) {
            List<CommentDto> commentDtos = commentRepository.findAllByItemId(item.getId())
                    .stream()
                    .map(commentMapper::toCommentDto)
                    .collect(Collectors.toList());
            Booking lastBooking = bookingRepository
                    .findFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(item.getId(), Booking.BookingStatus.APPROVED, now)
                    .orElse(null);
            Booking nextBooking = bookingRepository
                    .findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(item.getId(), Booking.BookingStatus.APPROVED, now)
                    .orElse(null);
            itemDto = itemMapper.toItemDto(item, commentDtos, lastBooking, nextBooking);
        } else {
            List<CommentDto> commentDtos = commentRepository.findAllByItemId(item.getId())
                    .stream()
                    .map(commentMapper::toCommentDto)
                    .collect(Collectors.toList());
            itemDto = itemMapper.toItemDto(item, commentDtos);
        }
        return itemDto;
    }

    public List<ItemDto> searchItems(String text, Integer from, Integer size) {
        log.info("Поиск вещей по запросу: {}", text);
        PageRequest request = PageRequest.of(from / size, size);
        return itemRepository.search(text, request).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public List<ItemDto> getAllItemByUser(Long userId, Integer from, Integer size) {
        log.info("Получение всех вещей пользователя с id: {}", userId);
        getUser(userId);
        LocalDateTime now = LocalDateTime.now();
        List<ItemDto> result = new ArrayList<>();
        PageRequest request = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.findAllByOwner_Id(userId, request);

        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());
        Map<Long, List<CommentDto>> commentsMap = commentRepository.findAllByItemIdIn(itemIds).stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.groupingBy(CommentDto::getItemId));

        Map<Long, Booking> lastBookingsMap  = bookingRepository
                .findAllByItemIdInAndStatusAndStartLessThanEqualOrderByStartDesc(itemIds, Booking.BookingStatus.APPROVED, now)
                .stream()
                .collect(Collectors.toMap(
                        o -> o.getItem().getId(),
                        b -> b,
                        (existing, replacement) -> existing
                ));

        Map<Long, Booking> nextBookingsMap = bookingRepository
                .findAllByItemIdInAndStatusAndStartAfterOrderByStartAsc(itemIds, Booking.BookingStatus.APPROVED, now)
                .stream()
                .collect(Collectors.toMap(
                        o -> o.getItem().getId(),
                        b -> b,
                        (existing, replacement) -> existing
                ));

        for (Item item : items) {
            if (item.getOwner().getId().equals(userId)) {
                result.add(itemMapper.toItemDto(item, commentsMap.get(item.getId()),
                        lastBookingsMap.get(item.getId()), nextBookingsMap.get(item.getId())));
            } else {
                result.add(itemMapper.toItemDto(item, commentsMap.get(item.getId())));
            }
        }
        return result;
    }

    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User user = getUser(userId);
        Item item = getItem(itemId);
        boolean hasBooked = bookingRepository.existsByBookerIdAndItemIdAndStatusAndStartBefore(
                userId, itemId, Booking.BookingStatus.APPROVED, LocalDateTime.now()
        );
        if (!hasBooked) {
            throw new IllegalArgumentException("Пользователь не может оставить отзыв к вещи, которую не арендовал");
        }

        Comment saved = commentRepository.save(commentMapper.toComment(commentDto, item, user));
        return commentMapper.toCommentDto(saved);
    }

    private Item getItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + id + " не найдена"));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }


    private ItemRequest getRequest(Long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден"));
    }
}
