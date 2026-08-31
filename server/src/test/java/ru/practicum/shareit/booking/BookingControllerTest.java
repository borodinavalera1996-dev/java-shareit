package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingInputDto bookingInputDto;
    private BookingDto bookingDto;
    private final String headerName = "X-Sharer-User-Id";

    @BeforeEach
    void setUp() {
        bookingInputDto = new BookingInputDto();
        bookingInputDto.setItemId(1L);
        bookingInputDto.setStart(LocalDateTime.now().plusDays(1));
        bookingInputDto.setEnd(LocalDateTime.now().plusDays(2));

        bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStatus(BookingDto.BookingStatus.WAITING);
    }

    @Test
    void addNewBooking_whenValid_thenStatusCreatedAndReturnDto() throws Exception {
        Mockito.when(bookingService.create(any(BookingInputDto.class), eq(1L)))
                .thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header(headerName, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingInputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().name())));
    }

    @Test
    void approved_whenInvoked_thenStatusOkAndReturnDto() throws Exception {
        bookingDto.setStatus(BookingDto.BookingStatus.APPROVED);
        Mockito.when(bookingService.approved(eq(1L), eq(1L), eq(true)))
                .thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header(headerName, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void getBooking_whenInvoked_thenStatusOkAndReturnDto() throws Exception {
        Mockito.when(bookingService.get(1L, 1L)).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/{bookingId}", 1L)
                        .header(headerName, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class));
    }

    @Test
    void getBookings_whenInvoked_thenStatusOkAndReturnList() throws Exception {
        Mockito.when(bookingService.getAllByUserIdSortedByDate(eq(1L), eq("ALL"), eq(0), eq(10)))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header(headerName, 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class));
    }

    @Test
    void getBookingsByOwner_whenInvoked_thenStatusOkAndReturnList() throws Exception {
        Mockito.when(bookingService.getAllByOwnerSortedByDate(eq(1L), eq("FUTURE"), eq(0), eq(5)))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header(headerName, 1L)
                        .param("state", "FUTURE")
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class));
    }
}
