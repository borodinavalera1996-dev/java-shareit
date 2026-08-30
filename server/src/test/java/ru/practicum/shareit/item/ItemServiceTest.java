package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemService itemService;

    private User owner;
    private User booker;
    private Item item;
    private ItemDto itemDto;
    private UpdateItemDto updateItemDto;
    private CommentDto commentDto;
    private Comment comment;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Владелец");
        owner.setEmail("owner@mail.com");

        booker = new User();
        booker.setId(2L);
        booker.setName("Арендатор");
        booker.setEmail("booker@mail.com");

        item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Ударная дрель");
        item.setStatus(true);
        item.setOwner(owner);

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Ударная дрель");
        itemDto.setAvailable(true);

        updateItemDto = new UpdateItemDto();
        updateItemDto.setName("Новая дрель");

        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Супер!");

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Супер!");
        comment.setItem(item);
        comment.setAuthor(booker);
    }

    @Test
    void createItem_whenValid_thenSaveAndReturnDto() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        Mockito.when(itemMapper.toItem(any(ItemDto.class))).thenReturn(item);
        Mockito.when(itemRepository.save(any(Item.class))).thenReturn(item);
        Mockito.when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        ItemDto result = itemService.createItem(itemDto, 1L);

        assertNotNull(result);
        assertEquals(itemDto.getName(), result.getName());
        Mockito.verify(itemRepository, Mockito.times(1)).save(any(Item.class));
    }

    @Test
    void updateItem_whenUserIsOwner_thenUpdateAndReturnDto() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(itemRepository.save(any(Item.class))).thenReturn(item);
        Mockito.when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        ItemDto result = itemService.updateItem(updateItemDto, 1L, 1L);

        assertNotNull(result);
        Mockito.verify(itemRepository, Mockito.times(1)).save(item);
    }

    @Test
    void updateItem_whenUserNotOwner_thenThrowNotFoundException() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () ->
                itemService.updateItem(updateItemDto, 1L, 2L)
        );
        Mockito.verify(itemRepository, Mockito.never()).save(any(Item.class));
    }

    @Test
    void updateItem_whenAllFieldsValid_thenUpdateAllFields() {
        UpdateItemDto updateDto = new UpdateItemDto();
        updateDto.setName("Новое имя вещи");
        updateDto.setDescription("Новое описание вещи");
        updateDto.setAvailable(false);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        itemService.updateItem(updateDto, 1L, 1L);

        assertEquals("Новое имя вещи", item.getName());
        assertEquals("Новое описание вещи", item.getDescription());
        assertFalse(item.getStatus());
    }

    @Test
    void updateItem_whenAllFieldsAreNull_thenNoFieldsUpdated() {
        UpdateItemDto updateDto = new UpdateItemDto();
        updateDto.setName(null);
        updateDto.setDescription(null);
        updateDto.setAvailable(null);

        String originalName = item.getName();
        String originalDesc = item.getDescription();
        boolean originalStatus = item.getStatus();

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        itemService.updateItem(updateDto, 1L, 1L);

        assertEquals(originalName, item.getName());
        assertEquals(originalDesc, item.getDescription());
        assertEquals(originalStatus, item.getStatus());
    }

    @Test
    void updateItem_whenFieldsAreBlank_thenNoFieldsUpdated() {
        UpdateItemDto updateDto = new UpdateItemDto();
        updateDto.setName("   ");
        updateDto.setDescription("");
        updateDto.setAvailable(true);

        String originalName = item.getName();
        String originalDesc = item.getDescription();

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        itemService.updateItem(updateDto, 1L, 1L);

        assertEquals(originalName, item.getName());
        assertEquals(originalDesc, item.getDescription());
        assertTrue(item.getStatus());
    }


    @Test
    void getItemById_whenUserIsOwner_thenReturnWithBookings() {
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(commentRepository.findAllByItemId(1L)).thenReturn(Collections.emptyList());
        Mockito.when(bookingRepository.findFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(Optional.empty());
        Mockito.when(bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(anyLong(), any(), any()))
                .thenReturn(Optional.empty());
        Mockito.when(itemMapper.toItemDto(eq(item), anyList(), any(), any())).thenReturn(itemDto);

        ItemDto result = itemService.getItemById(1L, 1L);

        assertNotNull(result);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getItemById_whenUserNotOwner_thenReturnWithoutBookings() {
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(commentRepository.findAllByItemId(1L)).thenReturn(Collections.emptyList());
        Mockito.when(itemMapper.toItemDto(eq(item), anyList())).thenReturn(itemDto);

        ItemDto result = itemService.getItemById(1L, 2L);

        assertNotNull(result);
        Mockito.verify(bookingRepository, Mockito.never())
                .findFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void searchItems_whenTextBlank_thenReturnEmptyList() {
        List<ItemDto> result = itemService.searchItems("   ", 0, 10);
        assertTrue(result.isEmpty());
        Mockito.verify(itemRepository, Mockito.never()).search(anyString(), any());
    }

    @Test
    void searchItems_whenTextValid_thenReturnMappedList() {
        Mockito.when(itemRepository.search(eq("дрель"), any())).thenReturn(List.of(item));
        Mockito.when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        List<ItemDto> result = itemService.searchItems("дрель", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void addComment_whenUserHasNotBooked_thenThrowIllegalArgumentException() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndStartBefore(anyLong(), anyLong(), any(), any()))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                itemService.addComment(2L, 1L, commentDto)
        );
        Mockito.verify(commentRepository, Mockito.never()).save(any(Comment.class));
    }

    @Test
    void addComment_whenUserHasBooked_thenSaveAndReturnComment() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndStartBefore(anyLong(), anyLong(), any(), any()))
                .thenReturn(true);
        Mockito.when(commentMapper.toComment(any(CommentDto.class), eq(item), eq(booker))).thenReturn(comment);
        Mockito.when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        Mockito.when(commentMapper.toCommentDto(any(Comment.class))).thenReturn(commentDto);

        CommentDto result = itemService.addComment(2L, 1L, commentDto);

        assertNotNull(result);
        assertEquals(commentDto.getText(), result.getText());
        Mockito.verify(commentRepository, Mockito.times(1)).save(any(Comment.class));
    }

    @Test
    void getAllItemByUser_whenInvoked_thenReturnItemsWithBookingsAndComments() {
        Long userId = 1L;
        Mockito.when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(owner));

        Mockito.when(itemRepository.findAllByOwner_Id(eq(userId), any()))
                .thenReturn(List.of(item));

        CommentDto commentDto = new CommentDto();
        commentDto.setItemId(item.getId());
        commentDto.setText("Тестовый комментарий");
        Mockito.when(commentRepository.findAllByItemIdIn(anyList())).thenReturn(List.of(new Comment()));
        Mockito.when(commentMapper.toCommentDto(any())).thenReturn(commentDto);

        Booking lastBooking = new Booking();
        lastBooking.setItem(item);
        Booking nextBooking = new Booking();
        nextBooking.setItem(item);

        Mockito.when(bookingRepository.findAllByItemIdInAndStatusAndStartLessThanEqualOrderByStartDesc(anyList(), any(), any()))
                .thenReturn(List.of(lastBooking));
        Mockito.when(bookingRepository.findAllByItemIdInAndStatusAndStartAfterOrderByStartAsc(anyList(), any(), any()))
                .thenReturn(List.of(nextBooking));

        Mockito.when(itemMapper.toItemDto(eq(item), anyList(), eq(lastBooking), eq(nextBooking)))
                .thenReturn(itemDto);

        List<ItemDto> result = itemService.getAllItemByUser(userId, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        Mockito.verify(itemRepository, Mockito.times(1)).findAllByOwner_Id(eq(userId), any());
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemIdInAndStatusAndStartLessThanEqualOrderByStartDesc(anyList(), any(), any());
    }

    @Test
    void getAllItemByUser_whenItemHasDifferentOwner_thenExecuteElseBranch() {
        Long userId = 1L;
        Mockito.when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(owner));

        User alternativeOwner = new User();
        alternativeOwner.setId(99L);
        Item foreignItem = new Item();
        foreignItem.setId(2L);
        foreignItem.setOwner(alternativeOwner);

        Mockito.when(itemRepository.findAllByOwner_Id(eq(userId), any())).thenReturn(List.of(foreignItem));
        Mockito.when(commentRepository.findAllByItemIdIn(anyList())).thenReturn(Collections.emptyList());
        Mockito.when(bookingRepository.findAllByItemIdInAndStatusAndStartLessThanEqualOrderByStartDesc(anyList(), any(), any()))
                .thenReturn(Collections.emptyList());
        Mockito.when(bookingRepository.findAllByItemIdInAndStatusAndStartAfterOrderByStartAsc(anyList(), any(), any()))
                .thenReturn(Collections.emptyList());

        Mockito.when(itemMapper.toItemDto(eq(foreignItem), any())).thenReturn(itemDto);

        List<ItemDto> result = itemService.getAllItemByUser(userId, 0, 10);

        assertNotNull(result);
        Mockito.verify(itemMapper, Mockito.times(1)).toItemDto(eq(foreignItem), any());
    }

}
