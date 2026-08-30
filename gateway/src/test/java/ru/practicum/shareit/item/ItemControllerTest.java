package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@ContextConfiguration(classes = ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    private ItemDto itemDto;
    private UpdateItemDto updateItemDto;
    private CommentDto commentDto;
    private ResponseEntity<Object> responseEntity;
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

        responseEntity = new ResponseEntity<>(itemDto, HttpStatus.OK);
    }

    @Test
    void createItem_whenValid_thenStatusOk() throws Exception {
        Mockito.when(itemClient.createItem(any(ItemDto.class), eq(1L)))
                .thenReturn(new ResponseEntity<>(itemDto, HttpStatus.CREATED));

        mockMvc.perform(post("/items")
                        .header(headerName, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateItem_whenValid_thenStatusOk() throws Exception {
        Mockito.when(itemClient.updateItem(any(UpdateItemDto.class), eq(1L), eq(1L)))
                .thenReturn(responseEntity);

        mockMvc.perform(patch("/items/{id}", 1L)
                        .header(headerName, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateItemDto)))
                .andExpect(status().isOk());
    }

    @Test
    void getItemById_whenInvoked_thenStatusOk() throws Exception {
        Mockito.when(itemClient.getItemById(eq(1L), eq(1L))).thenReturn(responseEntity);

        mockMvc.perform(get("/items/{id}", 1L)
                        .header(headerName, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_withValidParams_thenStatusOk() throws Exception {
        Mockito.when(itemClient.searchItems(anyString(), anyInt(), anyInt())).thenReturn(responseEntity);

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_withInvalidFrom_thenStatusBadRequest() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", "дрель")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchItems_withInvalidSize_thenStatusBadRequest() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", "дрель")
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllItems_withValidParams_thenStatusOk() throws Exception {
        Mockito.when(itemClient.getAllItemByUser(eq(1L), anyInt(), anyInt())).thenReturn(responseEntity);

        mockMvc.perform(get("/items")
                        .header(headerName, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_whenValid_thenStatusOk() throws Exception {
        Mockito.when(itemClient.addComment(eq(1L), eq(1L), any(CommentDto.class)))
                .thenReturn(new ResponseEntity<>(commentDto, HttpStatus.OK));

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header(headerName, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());
    }
}
