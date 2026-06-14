package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDto userDto;
    private UpdateUserDto updateUserDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Ivan");
        user.setEmail("ivan@mail.com");

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Ivan");
        userDto.setEmail("ivan@mail.com");

        updateUserDto = new UpdateUserDto();
        updateUserDto.setName("Ivan Updated");
        updateUserDto.setEmail("ivan_new@mail.com");
    }

    @Test
    void createUser_whenValid_thenReturnsSavedUser() {
        Mockito.when(userMapper.toUser(any(UserDto.class))).thenReturn(user);
        Mockito.when(userRepository.createUser(any(User.class))).thenReturn(user);
        Mockito.when(userMapper.toUserDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId());
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());
        Mockito.verify(userRepository, Mockito.times(1)).createUser(any(User.class));
    }

    @Test
    void getAllUsers_whenUsersExist_thenReturnsList() {
        Mockito.when(userRepository.getAllUsers()).thenReturn(List.of(user));
        Mockito.when(userMapper.toUserDto(user)).thenReturn(userDto);

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userDto.getId(), result.get(0).getId());
    }

    @Test
    void getAllUsers_whenEmpty_thenReturnsEmptyList() {
        Mockito.when(userRepository.getAllUsers()).thenReturn(Collections.emptyList());

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUserById_whenUserExists_thenReturnsUser() {
        Mockito.when(userRepository.getUserById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userMapper.toUserDto(user)).thenReturn(userDto);

        UserDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Ivan", result.getName());
    }

    @Test
    void getUserById_whenUserDoesNotExist_thenThrowsNotFoundException() {
        Mockito.when(userRepository.getUserById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                userService.getUserById(99L)
        );

        assertEquals("Пользователь с id=99 не найден", exception.getMessage());
    }

    @Test
    void updateUser_whenAllFieldsPresent_thenUpdatesAllFields() {
        Mockito.when(userRepository.getUserById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.updateUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto updatedDto = new UserDto();
        updatedDto.setId(1L);
        updatedDto.setName("Ivan Updated");
        updatedDto.setEmail("ivan_new@mail.com");
        Mockito.when(userMapper.toUserDto(any(User.class))).thenReturn(updatedDto);

        UserDto result = userService.updateUser(1L, updateUserDto);

        assertNotNull(result);
        assertEquals("Ivan Updated", result.getName());
        assertEquals("ivan_new@mail.com", result.getEmail());
    }

    @Test
    void updateUser_whenFieldsAreNullOrBlank_thenDoesNotUpdate() {
        Mockito.when(userRepository.getUserById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.updateUser(any(User.class))).thenReturn(user);
        Mockito.when(userMapper.toUserDto(user)).thenReturn(userDto);

        UpdateUserDto partialUpdate = new UpdateUserDto();
        partialUpdate.setName("");
        partialUpdate.setEmail(null);

        UserDto result = userService.updateUser(1L, partialUpdate);

        assertNotNull(result);
        assertEquals("Ivan", result.getName());
        assertEquals("ivan@mail.com", result.getEmail());
    }

    @Test
    void updateUser_whenUserNotFound_thenThrowsNotFoundException() {
        Mockito.when(userRepository.getUserById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                userService.updateUser(99L, updateUserDto)
        );
    }

    @Test
    void deleteUser_whenUserExists_thenCallsRepositoryDelete() {
        Mockito.when(userRepository.getUserById(1L)).thenReturn(Optional.of(user));
        Mockito.doNothing().when(userRepository).deleteUser(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));

        Mockito.verify(userRepository, Mockito.times(1)).deleteUser(1L);
    }

    @Test
    void deleteUser_whenUserDoesNotExist_thenThrowsNotFoundException() {
        Mockito.when(userRepository.getUserById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                userService.deleteUser(99L)
        );

        Mockito.verify(userRepository, Mockito.never()).deleteUser(99L);
    }
}
