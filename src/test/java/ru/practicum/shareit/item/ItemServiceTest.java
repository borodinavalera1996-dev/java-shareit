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
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
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

    @InjectMocks
    private ItemService itemService;

    private User owner;
    private User booker;
    private Item item;
    private ItemDto itemDto;
    private UpdateItemDto updateItemDto;
    private Comment comment;
    private CommentDto commentDto;
    private Booking lastBooking;
    private Booking nextBooking;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");
        owner.setEmail("owner@mail.com");

        booker = new User();
        booker.setId(2L);
        booker.setName("Booker");
        booker.setEmail("booker@mail.com");

        item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Проводная");
        item.setStatus(true);
        item.setOwner(owner);

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Проводная");
        itemDto.setAvailable(true);

        updateItemDto = new UpdateItemDto();
        updateItemDto.setName("Новое имя");
        updateItemDto.setDescription("Новое описание");
        updateItemDto.setAvailable(false);

        comment = new Comment();
        comment.setId(1L);
        comment.setText("комментарий");
        comment.setItem(item);
        comment.setAuthor(booker);
        comment.setCreated(LocalDateTime.now());

        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("комментарий");
        commentDto.setItemId(1L);
        commentDto.setAuthorName("Booker");

        lastBooking = new Booking();
        lastBooking.setId(10L);
        lastBooking.setBooker(booker);

        nextBooking = new Booking();
        nextBooking.setId(11L);
        nextBooking.setBooker(booker);
    }

    @Test
    void createItem_whenUserExists_thenSaveAndReturnDto() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        Mockito.when(itemMapper.toItem(any(ItemDto.class))).thenReturn(item);
        Mockito.when(itemRepository.save(any(Item.class))).thenReturn(item);
        Mockito.when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        ItemDto result = itemService.createItem(itemDto, 1L);

        assertNotNull(result);
        assertEquals(itemDto.getId(), result.getId());
        assertEquals(itemDto.getName(), result.getName());
        Mockito.verify(itemRepository, Mockito.times(1)).save(any(Item.class));
    }

    @Test
    void updateItem_whenUserIsOwner_thenUpdateFields() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(itemRepository.save(any(Item.class))).thenReturn(item);
        Mockito.when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        ItemDto result = itemService.updateItem(updateItemDto, 1L, 1L);

        assertNotNull(result);
        Mockito.verify(itemRepository, Mockito.times(1)).save(item);
    }

    @Test
    void updateItem_whenUserIsNotOwner_thenThrowNotFoundException() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> itemService.updateItem(updateItemDto, 1L, 2L));
        Mockito.verify(itemRepository, Mockito.never()).save(any(Item.class));
    }

    @Test
    void getItemById_whenUserIsOwner_thenFillBookings() {
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(commentRepository.findAllByItemId(1L)).thenReturn(List.of(comment));
        Mockito.when(commentMapper.toCommentDto(any(Comment.class))).thenReturn(commentDto);
        Mockito.when(bookingRepository.findFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(Optional.of(lastBooking));
        Mockito.when(bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(anyLong(), any(), any()))
                .thenReturn(Optional.of(nextBooking));
        Mockito.when(itemMapper.toItemDto(eq(item), anyList(), eq(lastBooking), eq(nextBooking))).thenReturn(itemDto);

        ItemDto result = itemService.getItemById(1L, 1L);

        assertNotNull(result);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getItemById_whenUserIsNotOwner_thenReturnDtoWithoutBookings() {
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(commentRepository.findAllByItemId(1L)).thenReturn(List.of(comment));
        Mockito.when(commentMapper.toCommentDto(any(Comment.class))).thenReturn(commentDto);
        Mockito.when(itemMapper.toItemDto(eq(item), anyList())).thenReturn(itemDto);

        ItemDto result = itemService.getItemById(1L, 2L);

        assertNotNull(result);
        Mockito.verify(bookingRepository, Mockito.never())
                .findFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void searchItems_whenTextIsEmpty_thenReturnEmptyList() {
        List<ItemDto> result = itemService.searchItems("");
        assertTrue(result.isEmpty());

        result = itemService.searchItems(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllItemByUser_whenInvoked_thenOptimizeCommentsAndFillDto() {
        item.setId(1L);

        lastBooking.setItem(item);
        nextBooking.setItem(item);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.findAllByOwner_Id(1L)).thenReturn(List.of(item));
        Mockito.when(commentRepository.findAllByItemIdIn(anyList())).thenReturn(List.of(comment));
        Mockito.when(commentMapper.toCommentDto(any(Comment.class))).thenReturn(commentDto);
        Mockito.when(bookingRepository.findAllByItemIdInAndStatusAndStartLessThanEqualOrderByStartDesc(anyList(), any(), any()))
                .thenReturn(List.of(lastBooking));
        Mockito.when(bookingRepository.findAllByItemIdInAndStatusAndStartAfterOrderByStartAsc(anyList(), any(), any()))
                .thenReturn(List.of(nextBooking));
        Mockito.when(itemMapper.toItemDto(eq(item), any(), eq(lastBooking), eq(nextBooking))).thenReturn(itemDto);

        List<ItemDto> result = itemService.getAllItemByUser(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        Mockito.verify(commentRepository, Mockito.times(1)).findAllByItemIdIn(anyList());
    }

    @Test
    void addComment_whenUserHasBooked_thenSaveComment() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndStartBefore(anyLong(), anyLong(), any(), any()))
                .thenReturn(true);
        Mockito.when(commentMapper.toComment(any(CommentDto.class), any(Item.class), any(User.class))).thenReturn(comment);
        Mockito.when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        Mockito.when(commentMapper.toCommentDto(any(Comment.class))).thenReturn(commentDto);

        CommentDto result = itemService.addComment(2L, 1L, commentDto);

        assertNotNull(result);
        assertEquals(commentDto.getText(), result.getText());
        Mockito.verify(commentRepository, Mockito.times(1)).save(any(Comment.class));
    }

    @Test
    void addComment_whenUserHasNotBooked_thenThrowIllegalArgumentException() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndStartBefore(anyLong(), anyLong(), any(), any()))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> itemService.addComment(2L, 1L, commentDto));
        Mockito.verify(commentRepository, Mockito.never()).save(any(Comment.class));
    }
}
