package ru.practicum.shareit.user;

import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User createUser(User user);

    List<User> getAllUsers();

    Optional<User> getUserById(Long id);

    User updateUser(User user);

    void deleteUser(Long id) throws NotFoundException;
}
