package ru.practicum.shareit.user;

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
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserClientTest {

    @Mock
    private RestTemplate restTemplate;

    private UserClient userClient;

    @Captor
    private ArgumentCaptor<HttpEntity<?>> httpEntityCaptor;

    private UserDto userDto;
    private UpdateUserDto updateUserDto;
    private ResponseEntity<Object> expectedResponse;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Ivan");
        userDto.setEmail("ivan@mail.com");

        updateUserDto = new UpdateUserDto();
        updateUserDto.setName("IvanUpdated");
        updateUserDto.setEmail("ivan_new@mail.com");

        expectedResponse = new ResponseEntity<>(userDto, HttpStatus.OK);

        RestTemplateBuilder builder = Mockito.mock(RestTemplateBuilder.class);
        Mockito.when(builder.uriTemplateHandler(any())).thenReturn(builder);
        Mockito.when(builder.requestFactory(any(java.util.function.Supplier.class))).thenReturn(builder);
        Mockito.when(builder.build()).thenReturn(restTemplate);

        userClient = new UserClient("http://localhost:8080", builder);
    }

    @Test
    void createUser_whenInvoked_thenCallPost() {
        Mockito.when(restTemplate.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = userClient.createUser(userDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(restTemplate).exchange(eq(""), eq(HttpMethod.POST), httpEntityCaptor.capture(), eq(Object.class));
        assertEquals(userDto, httpEntityCaptor.getValue().getBody());
    }

    @Test
    void getAllUsers_whenInvoked_thenCallGetWithParams() {
        Mockito.when(restTemplate.exchange(eq("?from={from}&size={size}"), eq(HttpMethod.GET), any(), eq(Object.class), anyMap()))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = userClient.getAllUsers(0, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(restTemplate).exchange(
                eq("?from={from}&size={size}"),
                eq(HttpMethod.GET),
                any(),
                eq(Object.class),
                eq(Map.of("from", 0, "size", 10))
        );
    }

    @Test
    void getUserById_whenInvoked_thenCallGet() {
        Mockito.when(restTemplate.exchange(eq("/1"), eq(HttpMethod.GET), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = userClient.getUserById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(restTemplate).exchange(eq("/1"), eq(HttpMethod.GET), any(), eq(Object.class));
    }

    @Test
    void updateUser_whenInvoked_thenCallPatch() {
        Mockito.when(restTemplate.exchange(eq("/1"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = userClient.updateUser(1L, updateUserDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mockito.verify(restTemplate).exchange(eq("/1"), eq(HttpMethod.PATCH), httpEntityCaptor.capture(), eq(Object.class));
        assertEquals(updateUserDto, httpEntityCaptor.getValue().getBody());
    }

    @Test
    void deleteUser_whenInvoked_thenCallDelete() {
        Mockito.when(restTemplate.exchange(eq("/1"), eq(HttpMethod.DELETE), any(), eq(Object.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertDoesNotThrow(() -> userClient.deleteUser(1L));
        Mockito.verify(restTemplate).exchange(eq("/1"), eq(HttpMethod.DELETE), any(), eq(Object.class));
    }
}
