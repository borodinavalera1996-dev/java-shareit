package ru.practicum.shareit.user;

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
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@ContextConfiguration(classes = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfig {
    }

    @MockBean
    private UserClient userClient;

    private UserDto userDto;
    private UpdateUserDto updateUserDto;
    private ResponseEntity<Object> responseEntity;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Ivan");
        userDto.setEmail("ivan@mail.com");

        updateUserDto = new UpdateUserDto();
        updateUserDto.setName("IvanUpdated");
        updateUserDto.setEmail("ivan_new@mail.com");

        responseEntity = new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @Test
    void createUser_whenValid_thenStatusOk() throws Exception {
        Mockito.when(userClient.createUser(any(UserDto.class)))
                .thenReturn(new ResponseEntity<>(userDto, HttpStatus.CREATED));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void getAllUsers_withValidParams_thenStatusOk() throws Exception {
        Mockito.when(userClient.getAllUsers(anyInt(), anyInt())).thenReturn(responseEntity);

        mockMvc.perform(get("/users")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_withInvalidFrom_thenStatusBadRequest() throws Exception {
        mockMvc.perform(get("/users")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUsers_withInvalidSize_thenStatusBadRequest() throws Exception {
        mockMvc.perform(get("/users")
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_whenInvoked_thenStatusOk() throws Exception {
        Mockito.when(userClient.getUserById(1L)).thenReturn(responseEntity);

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_whenValid_thenStatusOk() throws Exception {
        Mockito.when(userClient.updateUser(eq(1L), any(UpdateUserDto.class))).thenReturn(responseEntity);

        mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_whenInvoked_thenStatusNoContent() throws Exception {
        Mockito.doNothing().when(userClient).deleteUser(1L);

        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
