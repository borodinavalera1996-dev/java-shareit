package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestService {

    private final ItemRequestMapper itemRequestMapper;
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public ItemRequestDto createRequest(ItemRequestDto itemRequestDto, Long userId) {
        log.info("Создание запроса: {}", itemRequestDto);
        User requestor = getUser(userId);
        ItemRequest itemRequest = itemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest = itemRequestRepository.save(itemRequest);
        return itemRequestMapper.toItemRequestDto(itemRequest);
    }

    public List<ItemRequestDto> getSortedRequestsByOwner(Long userId, Integer from, Integer size) {
        log.info("Получение всех запросов пользователя с id: {}", userId);
        getUser(userId);
        PageRequest request = PageRequest.of(from / size, size, Sort.by("created").descending());
        List<ItemRequest> itemRequestList = itemRequestRepository.findAllByRequestorId(userId, request);

        List<Long> ids = itemRequestList.stream().map(ItemRequest::getId).toList();
        Map<Long, List<Item>> items = itemRepository.findAllByRequestIdIn(ids)
                .stream()
                .filter(item -> item.getRequest() != null)
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return itemRequestList.stream().map(itemRequest ->
                        itemRequestMapper.toItemRequestDto(itemRequest, items.get(itemRequest.getId())))
                .collect(Collectors.toList());
    }

    public List<ItemRequestDto> getSortedRequests(Long userId, Integer from, Integer size) {
        log.info("Получение всех запросов кроме собственных с id: {}", userId);
        getUser(userId);

        PageRequest request = PageRequest.of(from / size, size, Sort.by("created").descending());
        List<ItemRequest> itemRequestList = itemRequestRepository.findAllByRequestorIdNot(userId, request);

        List<Long> ids = itemRequestList.stream().map(ItemRequest::getId).toList();

        Map<Long, List<Item>> items = itemRepository.findAllByRequestIdIn(ids)
                .stream()
                .filter(item -> item.getRequest() != null)
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return itemRequestList.stream().map(itemRequest ->
                        itemRequestMapper.toItemRequestDto(itemRequest, items.get(itemRequest.getId())))
                .collect(Collectors.toList());
    }

    public ItemRequestDto getRequest(Long requestId) {
        log.info("Получение запроса с id: {}", requestId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден"));

        Map<Long, List<Item>> items = itemRepository.findAllByRequestIdIn(List.of(requestId))
                .stream()
                .filter(item -> item.getRequest() != null)
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return itemRequestMapper.toItemRequestDto(itemRequest, items.get(requestId));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }
}
