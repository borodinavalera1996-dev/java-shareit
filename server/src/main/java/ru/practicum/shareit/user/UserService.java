package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.info("Создание пользователя: {}", userDto);
        User user = userMapper.toUser(userDto);
        user = userRepository.save(user);
        return userMapper.toUserDto(user);
    }

    public List<UserDto> getAllUsers(Integer from, Integer size) {
        log.info("Получение всех пользователей");
        return userRepository.findAll(PageRequest.of(from / size, size)).stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        log.info("Получение пользователя по id: {}", id);
        User user = getUser(id);
        return userMapper.toUserDto(user);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    @Transactional
    public UserDto updateUser(Long id, UpdateUserDto userDto) {
        log.info("Обновление пользователя с id: {}, данные: {}", id, userDto);

        User existingUser = getUser(id);
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            existingUser.setEmail(userDto.getEmail());
        }
        User updatedUser = userRepository.save(existingUser);
        return userMapper.toUserDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Удаление пользователя по id: {}", id);
        userRepository.deleteById(id);
    }
}
