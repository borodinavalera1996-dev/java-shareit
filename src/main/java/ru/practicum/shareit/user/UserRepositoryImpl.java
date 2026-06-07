package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class UserRepositoryImpl implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User createUser(User user) {
        boolean emailExists = users.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()));

        if (emailExists) {
            log.warn("Попытка создания пользователя с уже существующим email: {}", user.getEmail());
            throw new ConflictException("Пользователь с email " + user.getEmail() + " уже существует");
        }
        long id = getNextId();
        user.setId(id);
        users.put(id, user);
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return List.copyOf(users.values());
    }

    @Override
    public Optional<User> getUserById(Long id) {
        if (users.containsKey(id))
            return Optional.of(users.get(id));
        return Optional.empty();
    }

    @Override
    public User updateUser(User userNew) {
        boolean emailExists = users.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(userNew.getEmail()) && !u.getId().equals(userNew.getId()));

        if (emailExists) {
            log.warn("Попытка обновить email на уже существующий: {}", userNew.getEmail());
            throw new ConflictException("Email " + userNew.getEmail() + " уже занят другим пользователем");
        }
        users.put(userNew.getId(), userNew);
        return userNew;
    }

    @Override
    public void deleteUser(Long id) throws NotFoundException {
        users.remove(id);
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
