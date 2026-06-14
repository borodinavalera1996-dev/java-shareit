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
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

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
        ItemDto updatedItemDto = new ItemDto();
        updatedItemDto.setId(1L);
        updatedItemDto.setName(updateItemDto.getName());
        updatedItemDto.setDescription(updateItemDto.getDescription());
        updatedItemDto.setAvailable(updateItemDto.getAvailable());

        Mockito.when(itemService.updateItem(any(UpdateItemDto.class), eq(1L), eq(1L)))
                .thenReturn(updatedItemDto);

        mockMvc.perform(patch("/items/{id}", 1L)
                        .header(headerName, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateItemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(updateItemDto.getName())))
                .andExpect(jsonPath("$.description", is(updateItemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(updateItemDto.getAvailable())));
    }

    @Test
    void getItemById_whenFound_thenStatusOk() throws Exception {
        Mockito.when(itemService.getItemById(1L)).thenReturn(itemDto);

        mockMvc.perform(get("/items/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())));
    }

    @Test
    void getItemById_whenNotFound_thenStatusNotFound() throws Exception {
        Mockito.when(itemService.getItemById(99L))
                .thenThrow(new NotFoundException("Вещь не найдена"));

        mockMvc.perform(get("/items/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchItems_whenTextParamPresent_thenStatusOkAndReturnsList() throws Exception {
        Mockito.when(itemService.searchItems("дрель")).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())));
    }

    @Test
    void getAllItems_whenHeaderPresent_thenStatusOkAndReturnsList() throws Exception {
        Mockito.when(itemService.getAllItemByUser(1L)).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header(headerName, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class));
    }
}
