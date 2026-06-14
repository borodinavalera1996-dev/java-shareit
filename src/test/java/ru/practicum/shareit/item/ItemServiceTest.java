package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemService itemService;

    private User owner;
    private Item item;
    private ItemDto itemDto;
    private UpdateItemDto updateItemDto;

    @BeforeEach
    void setUp() {
        owner = new User();

        owner.setId(1L);
        owner.setName("Ivan");
        owner.setEmail("ivan@mail.com");

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
    }

    @Test
    void createItem_whenUserExists_thenReturnsSavedItem() {
        Mockito.when(userRepository.getUserById(1L)).thenReturn(Optional.of(owner));
        Mockito.when(itemMapper.toItem(any(ItemDto.class))).thenReturn(item);
        Mockito.when(itemRepository.createItem(any(Item.class))).thenReturn(item);
        Mockito.when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        ItemDto result = itemService.createItem(itemDto, 1L);

        assertNotNull(result);
        assertEquals(itemDto.getId(), result.getId());
        assertEquals(itemDto.getName(), result.getName());
        Mockito.verify(itemRepository, Mockito.times(1)).createItem(any(Item.class));
    }

    @Test
    void createItem_whenUserDoesNotExist_thenThrowsNotFoundException() {
        Mockito.when(userRepository.getUserById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createItem(itemDto, 99L));
        Mockito.verify(itemRepository, Mockito.never()).createItem(any(Item.class));
    }

    @Test
    void updateItem_whenAllFieldsPresentAndUserIsOwner_thenUpdatesAllFields() {
        Mockito.when(userRepository.getUserById(1L)).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.getItemById(1L)).thenReturn(Optional.of(item));
        Mockito.when(itemRepository.updateItem(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemDto updatedDto = new ItemDto();
        updatedDto.setId(1L);
        updatedDto.setName("Новое имя");
        updatedDto.setDescription("Новое описание");
        updatedDto.setAvailable(false);
        Mockito.when(itemMapper.toItemDto(any(Item.class))).thenReturn(updatedDto);

        ItemDto result = itemService.updateItem(updateItemDto, 1L, 1L);

        assertNotNull(result);
        assertEquals("Новое имя", result.getName());
        assertEquals("Новое описание", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void updateItem_whenUserIsNotOwner_thenThrowsNotFoundException() {
        Mockito.when(userRepository.getUserById(2L)).thenReturn(Optional.of(new User()));
        Mockito.when(itemRepository.getItemById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> itemService.updateItem(updateItemDto, 1L, 2L));
        Mockito.verify(itemRepository, Mockito.never()).updateItem(any(Item.class));
    }

    @Test
    void updateItem_whenFieldsAreNullOrBlank_thenDoesNotUpdate() {
        Mockito.when(userRepository.getUserById(1L)).thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.getItemById(1L)).thenReturn(Optional.of(item));
        Mockito.when(itemRepository.updateItem(any(Item.class))).thenReturn(item);
        Mockito.when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        UpdateItemDto partialUpdate = new UpdateItemDto();
        partialUpdate.setName(" ");
        partialUpdate.setDescription(null);
        partialUpdate.setAvailable(null);

        ItemDto result = itemService.updateItem(partialUpdate, 1L, 1L);

        assertNotNull(result);
        assertEquals("Дрель", result.getName());
        assertEquals("Проводная", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void getItemById_whenItemExists_thenReturnsItem() {
        Mockito.when(itemRepository.getItemById(1L)).thenReturn(Optional.of(item));
        Mockito.when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.getItemById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Дрель", result.getName());
    }

    @Test
    void getItemById_whenItemDoesNotExist_thenThrowsNotFoundException() {
        Mockito.when(itemRepository.getItemById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItemById(99L));
    }

    @Test
    void searchItems_whenTextIsNotBlank_thenReturnsList() {
        Mockito.when(itemRepository.searchItems("дрель")).thenReturn(List.of(item));
        Mockito.when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.searchItems("дрель");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Дрель", result.get(0).getName());
    }

    @Test
    void searchItems_whenTextIsBlank_thenReturnsEmptyList() {
        List<ItemDto> resultEmpty = itemService.searchItems("");
        List<ItemDto> resultNull = itemService.searchItems(null);

        assertNotNull(resultEmpty);
        assertTrue(resultEmpty.isEmpty());
        assertNotNull(resultNull);
        assertTrue(resultNull.isEmpty());

        Mockito.verify(itemRepository, Mockito.never()).searchItems(any());
    }

    @Test
    void getAllItemByUser_whenItemsExist_thenReturnsList() {
        Mockito.when(itemRepository.getAllItemByUser(1L)).thenReturn(List.of(item));
        Mockito.when(userRepository.getUserById(1L)).thenReturn(Optional.of(new User()));
        Mockito.when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.getAllItemByUser(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}
