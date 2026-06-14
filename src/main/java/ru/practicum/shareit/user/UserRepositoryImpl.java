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
        checkEmail(user);
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
        checkEmail(userNew);
        users.put(userNew.getId(), userNew);
        return userNew;
    }

    @Override
    public void deleteUser(Long id) throws NotFoundException {
        users.remove(id);
    }

    private void checkEmail(User userNew) {
        boolean emailExists = users.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(userNew.getEmail()) && !u.getId().equals(userNew.getId()));

        if (emailExists) {
            log.warn("Попытка обновить или создать пользователя с email, который уже занят другим пользователем: {}", userNew.getEmail());
            throw new ConflictException("Email " + userNew.getEmail() + " уже занят другим пользователем");
        }
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
