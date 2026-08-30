package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@ContextConfiguration(classes = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    private BookItemRequestDto bookItemRequestDto;
    private ResponseEntity<Object> responseEntity;
    private final String headerName = "X-Sharer-User-Id";

    @BeforeEach
    void setUp() {
        bookItemRequestDto = new BookItemRequestDto();

        responseEntity = new ResponseEntity<>(bookItemRequestDto, HttpStatus.OK);
    }

    @Test
    void getBookings_withValidParams_thenStatusOk() throws Exception {
        Mockito.when(bookingClient.getBookings(eq(1L), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(responseEntity);

        mockMvc.perform(get("/bookings")
                        .header(headerName, 1L)
                        .param("state", "all")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getBookings_withUnknownState_thenStatusBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(headerName, 1L)
                        .param("state", "unsupported_state"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookings_withInvalidFrom_thenStatusBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(headerName, 1L)
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookings_withInvalidSize_thenStatusBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(headerName, 1L)
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookItem_whenValid_thenStatusOk() throws Exception {
        Mockito.when(bookingClient.bookItem(eq(1L), any(BookItemRequestDto.class)))
                .thenReturn(new ResponseEntity<>(bookItemRequestDto, HttpStatus.CREATED));

        mockMvc.perform(post("/bookings")
                        .header(headerName, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookItemRequestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void getBooking_whenInvoked_thenStatusOk() throws Exception {
        Mockito.when(bookingClient.getBooking(1L, 1L)).thenReturn(responseEntity);

        mockMvc.perform(get("/bookings/{bookingId}", 1L)
                        .header(headerName, 1L))
                .andExpect(status().isOk());
    }
}
