package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.User;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper userMapper;
    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();

        user = new User();
        user.setId(1L);
        user.setName("Иван");
        user.setEmail("ivan@mail.com");

        userDto = new UserDto();
        userDto.setId(2L);
        userDto.setName("Петр");
        userDto.setEmail("petr@mail.com");
    }

    @Test
    void toUserDto_whenValidUser_thenReturnCorrectDto() {
        UserDto result = userMapper.toUserDto(user);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void toUser_whenValidDto_thenReturnCorrectEntity() {
        User result = userMapper.toUser(userDto);

        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId());
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());
    }
}
