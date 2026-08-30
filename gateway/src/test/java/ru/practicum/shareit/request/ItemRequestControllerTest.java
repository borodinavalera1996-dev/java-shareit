package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@ContextConfiguration(classes = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    private ItemRequestDto itemRequestDto;
    private ResponseEntity<Object> responseEntity;
    private final String headerName = "X-Sharer-User-Id";

    @BeforeEach
    void setUp() {
        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужна дрель")
                .build();

        responseEntity = new ResponseEntity<>(itemRequestDto, HttpStatus.OK);
    }

    @Test
    void createRequest_whenValid_thenStatusOk() throws Exception {
        Mockito.when(itemRequestClient.createRequest(any(ItemRequestDto.class), eq(1L)))
                .thenReturn(new ResponseEntity<>(itemRequestDto, HttpStatus.CREATED));

        mockMvc.perform(post("/requests")
                        .header(headerName, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void getRequests_withValidParams_thenStatusOk() throws Exception {
        Mockito.when(itemRequestClient.getSortedRequestsByOwner(eq(1L), anyInt(), anyInt()))
                .thenReturn(responseEntity);

        mockMvc.perform(get("/requests")
                        .header(headerName, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getRequests_withInvalidFrom_thenStatusBadRequest() throws Exception {
        mockMvc.perform(get("/requests")
                        .header(headerName, 1L)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequests_withInvalidSize_thenStatusBadRequest() throws Exception {
        mockMvc.perform(get("/requests")
                        .header(headerName, 1L)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRequests_withValidParams_thenStatusOk() throws Exception {
        Mockito.when(itemRequestClient.getSortedRequests(eq(1L), anyInt(), anyInt()))
                .thenReturn(responseEntity);

        mockMvc.perform(get("/requests/all")
                        .header(headerName, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRequests_withInvalidFrom_thenStatusBadRequest() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header(headerName, 1L)
                        .param("from", "-5")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRequests_withInvalidSize_thenStatusBadRequest() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header(headerName, 1L)
                        .param("from", "0")
                        .param("size", "-10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequest_whenInvoked_thenStatusOk() throws Exception {
        Mockito.when(itemRequestClient.getRequest(1L)).thenReturn(responseEntity);

        mockMvc.perform(get("/requests/{requestId}", 1L))
                .andExpect(status().isOk());
    }
}
