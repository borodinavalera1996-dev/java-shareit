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

        bookingDto = BookingDto.builder()
                .id(1L)
                .start(bookingInputDto.getStart())
                .end(bookingInputDto.getEnd())
                .status(null)
                .build();
    }

    @Test
    void addNewBooking_whenValid_thenStatusCreatedAndReturnsBooking() throws Exception {
        Mockito.when(bookingService.create(any(BookingInputDto.class), eq(1L))).thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header(headerName, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingInputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class));
    }

    @Test
    void addNewBooking_whenPatchApproved_thenStatusOkAndReturnsUpdatedBooking() throws Exception {
        Mockito.when(bookingService.approved(eq(1L), eq(1L), eq(true))).thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header(headerName, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class));
    }

    @Test
    void getBooking_whenInvoked_thenStatusOkAndReturnsBooking() throws Exception {
        Mockito.when(bookingService.get(eq(1L), eq(1L))).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/{bookingId}", 1L)
                        .header(headerName, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class));
    }

    @Test
    void getBookings_whenDefaultState_thenStatusOkAndReturnsList() throws Exception {
        Mockito.when(bookingService.getAllByUserIdSortedByDate(eq(1L), eq("ALL"))).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header(headerName, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class));
    }

    @Test
    void getBookingsByOwner_whenCustomState_thenStatusOkAndReturnsList() throws Exception {
        Mockito.when(bookingService.getAllByOwnerSortedByDate(eq(1L), eq("FUTURE"))).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header(headerName, 1L)
                        .param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class));
    }
}
