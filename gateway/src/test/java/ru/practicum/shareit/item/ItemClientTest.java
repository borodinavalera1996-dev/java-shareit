package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemClientTest {

    @Mock
    private RestTemplate restTemplate;

    private ItemClient itemClient;

    @Captor
    private ArgumentCaptor<HttpEntity<?>> httpEntityCaptor;

    private ItemDto itemDto;
    private UpdateItemDto updateItemDto;
    private CommentDto commentDto;
    private ResponseEntity<Object> expectedResponse;
    private final String headerName = "X-Sharer-User-Id";

    @BeforeEach
    void setUp() {
        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Проводная дрель");
        itemDto.setAvailable(true);

        updateItemDto = new UpdateItemDto();
        updateItemDto.setName("Дрель1");
        updateItemDto.setDescription("Проводная дрель1");
        updateItemDto.setAvailable(false);

        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Отличный комментарий");

        expectedResponse = new ResponseEntity<>(itemDto, HttpStatus.OK);

        RestTemplateBuilder builder = Mockito.mock(RestTemplateBuilder.class);
        Mockito.when(builder.uriTemplateHandler(any())).thenReturn(builder);
        Mockito.when(builder.requestFactory(any(java.util.function.Supplier.class))).thenReturn(builder);
        Mockito.when(builder.build()).thenReturn(restTemplate);

        itemClient = new ItemClient("http://localhost:8080", builder);
    }

    @Test
    void createItem_whenInvoked_thenCallPostWithHeader() {
        Mockito.when(restTemplate.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemClient.createItem(itemDto, 1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(restTemplate).exchange(eq(""), eq(HttpMethod.POST), httpEntityCaptor.capture(), eq(Object.class));
        assertEquals(itemDto, httpEntityCaptor.getValue().getBody());
        assertEquals("1", httpEntityCaptor.getValue().getHeaders().getFirst(headerName));
    }

    @Test
    void updateItem_whenInvoked_thenCallPatchWithHeader() {
        Mockito.when(restTemplate.exchange(eq("/1"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemClient.updateItem(updateItemDto, 1L, 1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(restTemplate).exchange(eq("/1"), eq(HttpMethod.PATCH), httpEntityCaptor.capture(), eq(Object.class));
        assertEquals(updateItemDto, httpEntityCaptor.getValue().getBody());
        assertEquals("1", httpEntityCaptor.getValue().getHeaders().getFirst(headerName));
    }

    @Test
    void getItemById_whenInvoked_thenCallGetWithHeader() {
        Mockito.when(restTemplate.exchange(eq("/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemClient.getItemById(1L, 1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(restTemplate).exchange(eq("/1"), eq(HttpMethod.GET), httpEntityCaptor.capture(), eq(Object.class));
        assertEquals("1", httpEntityCaptor.getValue().getHeaders().getFirst(headerName));
    }

    @Test
    void searchItems_whenInvoked_thenCallGetWithParams() {
        Mockito.when(restTemplate.exchange(eq("/search?state={state}&from={from}&size={size}"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), anyMap()))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemClient.searchItems("дрель", 0, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(restTemplate).exchange(
                eq("/search?state={state}&from={from}&size={size}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                eq(Map.of("test", "дрель", "from", 0, "size", 10))
        );
    }

    @Test
    void getAllItemByUser_whenInvoked_thenCallGetWithParamsAndHeader() {
        Mockito.when(restTemplate.exchange(eq("?&from={from}&size={size}"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), anyMap()))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemClient.getAllItemByUser(1L, 0, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(restTemplate).exchange(
                eq("?&from={from}&size={size}"),
                eq(HttpMethod.GET),
                httpEntityCaptor.capture(),
                eq(Object.class),
                eq(Map.of("from", 0, "size", 10))
        );
        assertEquals("1", httpEntityCaptor.getValue().getHeaders().getFirst(headerName));
    }

    @Test
    void addComment_whenInvoked_thenCallPostWithItemIdAndHeader() {
        Mockito.when(restTemplate.exchange(eq("/1/comment"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemClient.addComment(1L, 1L, commentDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(restTemplate).exchange(eq("/1/comment"), eq(HttpMethod.POST), httpEntityCaptor.capture(), eq(Object.class));
        assertEquals(commentDto, httpEntityCaptor.getValue().getBody());
        assertEquals("1", httpEntityCaptor.getValue().getHeaders().getFirst(headerName));
    }
}
