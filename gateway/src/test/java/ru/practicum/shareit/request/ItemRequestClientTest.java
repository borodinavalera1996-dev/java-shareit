package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestClientTest {

    @Mock
    private RestTemplate restTemplate;

    private ItemRequestClient itemRequestClient;

    @Captor
    private ArgumentCaptor<HttpEntity<?>> httpEntityCaptor;

    private ItemRequestDto itemRequestDto;
    private ResponseEntity<Object> expectedResponse;
    private final String headerName = "X-Sharer-User-Id";

    @BeforeEach
    void setUp() {
        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужна дрель")
                .build();

        expectedResponse = new ResponseEntity<>(itemRequestDto, HttpStatus.OK);

        RestTemplateBuilder builder = Mockito.mock(RestTemplateBuilder.class);
        Mockito.when(builder.uriTemplateHandler(any())).thenReturn(builder);
        Mockito.when(builder.requestFactory(any(java.util.function.Supplier.class))).thenReturn(builder);
        Mockito.when(builder.build()).thenReturn(restTemplate);

        itemRequestClient = new ItemRequestClient("http://localhost:8080", builder);
    }

    @Test
    void createRequest_whenInvoked_thenCallPostWithHeader() {
        Mockito.when(restTemplate.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemRequestClient.createRequest(itemRequestDto, 1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(restTemplate).exchange(eq(""), eq(HttpMethod.POST), httpEntityCaptor.capture(), eq(Object.class));
        assertEquals(itemRequestDto, httpEntityCaptor.getValue().getBody());
        assertEquals("1", httpEntityCaptor.getValue().getHeaders().getFirst(headerName));
    }

    @Test
    void getSortedRequestsByOwner_whenInvoked_thenCallGetWithParamsAndHeader() {
        Mockito.when(restTemplate.exchange(eq("?from={from}&size={size}"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), anyMap()))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemRequestClient.getSortedRequestsByOwner(1L, 0, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(restTemplate).exchange(
                eq("?from={from}&size={size}"),
                eq(HttpMethod.GET),
                httpEntityCaptor.capture(),
                eq(Object.class),
                eq(Map.of("from", 0, "size", 10))
        );
        assertEquals("1", httpEntityCaptor.getValue().getHeaders().getFirst(headerName));
    }

    @Test
    void getSortedRequests_whenInvoked_thenCallGetWithAllPathAndHeader() {
        Mockito.when(restTemplate.exchange(eq("/all?from={from}&size={size}"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), anyMap()))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemRequestClient.getSortedRequests(1L, 0, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(restTemplate).exchange(
                eq("/all?from={from}&size={size}"),
                eq(HttpMethod.GET),
                httpEntityCaptor.capture(),
                eq(Object.class),
                eq(Map.of("from", 0, "size", 10))
        );
        assertEquals("1", httpEntityCaptor.getValue().getHeaders().getFirst(headerName));
    }

    @Test
    void getRequest_whenInvoked_thenCallGetWithPath() {
        Mockito.when(restTemplate.exchange(eq("/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemRequestClient.getRequest(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(restTemplate).exchange(eq("/1"), eq(HttpMethod.GET), httpEntityCaptor.capture(), eq(Object.class));
    }
}
