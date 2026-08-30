package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookingClientTest {

    @Mock
    private RestTemplate restTemplate;

    private BookingClient bookingClient;

    @Captor
    private ArgumentCaptor<HttpEntity<?>> httpEntityCaptor;

    private BookItemRequestDto bookItemRequestDto;
    private ResponseEntity<Object> expectedResponse;
    private final String headerName = "X-Sharer-User-Id";

    @BeforeEach
    void setUp() {
        bookItemRequestDto = new BookItemRequestDto();

        expectedResponse = new ResponseEntity<>(bookItemRequestDto, HttpStatus.OK);


        RestTemplateBuilder builder = Mockito.mock(RestTemplateBuilder.class);
        Mockito.when(builder.uriTemplateHandler(any())).thenReturn(builder);
        Mockito.when(builder.requestFactory(any(java.util.function.Supplier.class))).thenReturn(builder);
        Mockito.when(builder.build()).thenReturn(restTemplate);

        bookingClient = new BookingClient("http://localhost:8080", builder);
    }

    @Test
    void getBookings_whenInvoked_thenCallGetWithParamsAndHeader() {
        Mockito.when(restTemplate.exchange(eq("?state={state}&from={from}&size={size}"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), anyMap()))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = bookingClient.getBookings(1L, BookingState.ALL, 0, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(restTemplate).exchange(
                eq("?state={state}&from={from}&size={size}"),
                eq(HttpMethod.GET),
                httpEntityCaptor.capture(),
                eq(Object.class),
                eq(Map.of("state", "ALL", "from", 0, "size", 10))
        );
        assertEquals("1", httpEntityCaptor.getValue().getHeaders().getFirst(headerName));
    }

    @Test
    void bookItem_whenInvoked_thenCallPostWithHeader() {
        Mockito.when(restTemplate.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = bookingClient.bookItem(1L, bookItemRequestDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(restTemplate).exchange(eq(""), eq(HttpMethod.POST), httpEntityCaptor.capture(), eq(Object.class));
        assertEquals(bookItemRequestDto, httpEntityCaptor.getValue().getBody());
        assertEquals("1", httpEntityCaptor.getValue().getHeaders().getFirst(headerName));
    }

    @Test
    void getBooking_whenInvoked_thenCallGetWithBookingIdAndHeader() {
        Mockito.when(restTemplate.exchange(eq("/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = bookingClient.getBooking(1L, 1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(restTemplate).exchange(eq("/1"), eq(HttpMethod.GET), httpEntityCaptor.capture(), eq(Object.class));
        assertEquals("1", httpEntityCaptor.getValue().getHeaders().getFirst(headerName));
    }
}
