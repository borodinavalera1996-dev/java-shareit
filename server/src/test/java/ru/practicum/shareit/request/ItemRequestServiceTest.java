package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

    @Mock
    private ItemRequestMapper itemRequestMapper;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestService itemRequestService;

    private User user;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;
    private Item item;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Роман");
        user.setEmail("roman@mail.com");

        itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Нужна дрель");
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());

        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(1L);
        itemRequestDto.setDescription("Нужна дрель");

        item = new Item();
        item.setId(1L);
        item.setName("Аккумуляторная дрель");
        item.setRequest(itemRequest);
    }

    @Test
    void createRequest_whenUserExists_thenSaveAndReturnDto() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRequestMapper.toItemRequest(any(ItemRequestDto.class))).thenReturn(itemRequest);
        Mockito.when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);
        Mockito.when(itemRequestMapper.toItemRequestDto(any(ItemRequest.class))).thenReturn(itemRequestDto);

        ItemRequestDto result = itemRequestService.createRequest(itemRequestDto, 1L);

        assertNotNull(result);
        assertEquals(itemRequestDto.getId(), result.getId());
        assertEquals(itemRequestDto.getDescription(), result.getDescription());
        Mockito.verify(itemRequestRepository, Mockito.times(1)).save(any(ItemRequest.class));
    }

    @Test
    void createRequest_whenUserNotFound_thenThrowNotFoundException() {
        Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemRequestService.createRequest(itemRequestDto, 99L)
        );
        Mockito.verify(itemRequestRepository, Mockito.never()).save(any(ItemRequest.class));
    }

    @Test
    void getSortedRequestsByOwner_whenInvoked_thenReturnMappedList() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findAllByRequestorId(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(itemRequest));
        Mockito.when(itemRepository.findAllByRequestIdIn(anyList())).thenReturn(List.of(item));
        Mockito.when(itemRequestMapper.toItemRequestDto(eq(itemRequest), anyList())).thenReturn(itemRequestDto);

        List<ItemRequestDto> result = itemRequestService.getSortedRequestsByOwner(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(itemRequestDto.getId(), result.get(0).getId());
        Mockito.verify(itemRequestRepository, Mockito.times(1))
                .findAllByRequestorId(eq(1L), any(Pageable.class));
    }

    @Test
    void getSortedRequests_whenInvoked_thenReturnMappedList() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findAllByRequestorIdNot(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(itemRequest));
        Mockito.when(itemRepository.findAllByRequestIdIn(anyList())).thenReturn(List.of(item));
        Mockito.when(itemRequestMapper.toItemRequestDto(eq(itemRequest), anyList())).thenReturn(itemRequestDto);

        List<ItemRequestDto> result = itemRequestService.getSortedRequests(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        Mockito.verify(itemRequestRepository, Mockito.times(1))
                .findAllByRequestorIdNot(eq(1L), any(Pageable.class));
    }

    @Test
    void getRequest_whenRequestExists_thenReturnDto() {
        Mockito.when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));
        Mockito.when(itemRepository.findAllByRequestIdIn(anyList())).thenReturn(List.of(item));
        Mockito.when(itemRequestMapper.toItemRequestDto(eq(itemRequest), anyList())).thenReturn(itemRequestDto);

        ItemRequestDto result = itemRequestService.getRequest(1L);

        assertNotNull(result);
        assertEquals(itemRequestDto.getId(), result.getId());
        Mockito.verify(itemRequestRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    void getRequest_whenRequestNotFound_thenThrowNotFoundException() {
        Mockito.when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequest(99L));
        Mockito.verify(itemRepository, Mockito.never()).findAllByRequestIdIn(anyList());
    }
}
