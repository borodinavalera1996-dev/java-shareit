package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto;
    private UpdateItemDto updateItemDto;
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
    }

    @Test
    void createItem_whenValid_thenStatusCreatedAndReturnsItem() throws Exception {
        Mockito.when(itemService.createItem(any(ItemDto.class), eq(1L))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header(headerName, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }

    @Test
    void createItem_whenXSharerUserIdHeaderMissing_thenStatusBadRequest() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void updateItem_whenValid_thenStatusOkAndReturnsUpdatedItem() throws Exception {
        ItemDto updatedResult = new ItemDto();
        updatedResult.setId(1L);
        updatedResult.setName("Дрель1");
        updatedResult.setDescription("Проводная дрель1");
        updatedResult.setAvailable(false);

        Mockito.when(itemService.updateItem(any(UpdateItemDto.class), eq(1L), eq(1L)))
                .thenReturn(updatedResult);

        mockMvc.perform(patch("/items/{id}", 1L)
                        .header(headerName, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateItemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedResult.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updatedResult.getName())))
                .andExpect(jsonPath("$.description", is(updatedResult.getDescription())))
                .andExpect(jsonPath("$.available", is(updatedResult.getAvailable())));
    }

    @Test
    void getItemById_whenInvoked_thenStatusOkAndReturnsItem() throws Exception {
        Mockito.when(itemService.getItemById(1L, null)).thenReturn(itemDto);

        mockMvc.perform(get("/items/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }

    @Test
    void searchItems_whenTextProvided_thenStatusOkAndReturnsList() throws Exception {
        Mockito.when(itemService.searchItems("Дрель")).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "Дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())));
    }

    @Test
    void getAllItems_whenUserHasItems_thenStatusOkAndReturnsList() throws Exception {
        Mockito.when(itemService.getAllItemByUser(1L)).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header(headerName, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())));
    }

    @Test
    void addComment_whenValid_thenStatusOkAndReturnsComment() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Отличный инструмент!");
        commentDto.setAuthorName("Иван");
        commentDto.setCreated(LocalDateTime.now());

        Mockito.when(itemService.addComment(eq(1L), eq(1L), any(CommentDto.class)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header(headerName, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())));
    }
}
