package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

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
        updateUserDto.setName("IvanUpdated");
        updateUserDto.setEmail("ivan_new@mail.com");
    }

    @Test
    void createUser_whenValid_thenSaveAndReturnDto() {
        Mockito.when(userMapper.toUser(any(UserDto.class))).thenReturn(user);
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);
        Mockito.when(userMapper.toUserDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId());
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());
        Mockito.verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test
    void getAllUsers_whenInvoked_thenReturnList() {
        org.springframework.data.domain.Page<User> userPage =
                new org.springframework.data.domain.PageImpl<>(List.of(user));
        Mockito.when(userRepository.findAll(PageRequest.of(0, 10))).thenReturn(userPage);
        Mockito.when(userMapper.toUserDto(any(User.class))).thenReturn(userDto);

        List<UserDto> result = userService.getAllUsers(0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userDto.getId(), result.getFirst().getId());
        Mockito.verify(userRepository, Mockito.times(1)).findAll(PageRequest.of(0, 10));
    }

    @Test
    void getUserById_whenUserExists_thenReturnDto() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userMapper.toUserDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId());
        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    void getUserById_whenUserDoesNotExist_thenThrowNotFoundException() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(1L));
        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    void updateUser_whenUserExists_thenUpdateFieldsAndSave() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);
        Mockito.when(userMapper.toUserDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.updateUser(1L, updateUserDto);

        assertNotNull(result);
        assertEquals("IvanUpdated", user.getName());
        assertEquals("ivan_new@mail.com", user.getEmail());
        Mockito.verify(userRepository, Mockito.times(1)).save(user);
    }

    @Test
    void updateUser_whenUserDoesNotExist_thenThrowNotFoundException() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(1L, updateUserDto));
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void deleteUser_whenInvoked_thenDeleteById() {
        Mockito.doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));
        Mockito.verify(userRepository, Mockito.times(1)).deleteById(1L);
    }
}
