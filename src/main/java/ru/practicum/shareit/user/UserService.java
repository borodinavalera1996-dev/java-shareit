package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto createUser(@Valid UserDto userDto) {
        log.info("Создание пользователя: {}", userDto);
        User user = userMapper.toUser(userDto);
        user = userRepository.createUser(user);
        return userMapper.toUserDto(user);
    }

    public List<UserDto> getAllUsers() {
        log.info("Получение всех пользователей");
        return userRepository.getAllUsers().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        log.info("Получение пользователя по id: {}", id);
        User user = userRepository.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
        return userMapper.toUserDto(user);
    }

    public UserDto updateUser(Long id, @Valid UpdateUserDto userDto) {
        log.info("Обновление пользователя с id: {}, данные: {}", id, userDto);

        User existingUser = userRepository.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            existingUser.setEmail(userDto.getEmail());
        }
        User updatedUser = userRepository.updateUser(existingUser);
        return userMapper.toUserDto(updatedUser);
    }

    public void deleteUser(Long id) {
        log.info("Удаление пользователя по id: {}", id);
        if (userRepository.getUserById(id).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        userRepository.deleteUser(id);
    }
}
