package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ConflictException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryImplTest {

    private UserRepositoryImpl userRepository;
    private User user;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepositoryImpl();

        user = new User();
        user.setName("Ivan");
        user.setEmail("ivan@mail.com");
    }

    @Test
    void createUser_whenEmailIsUnique_thenSuccessAndGeneratesId() {
        User savedUser = userRepository.createUser(user);

        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        assertEquals(1L, savedUser.getId());
        assertEquals("Ivan", savedUser.getName());
    }

    @Test
    void createUser_whenEmailAlreadyExists_thenThrowsConflictException() {
        userRepository.createUser(user);

        User duplicateUser = new User();
        duplicateUser.setName("Petr");
        duplicateUser.setEmail("ivan@mail.com");

        ConflictException exception = assertThrows(ConflictException.class, () ->
                userRepository.createUser(duplicateUser)
        );
        assertTrue(exception.getMessage().contains("уже существует"));
    }

    @Test
    void createUser_whenEmailExistsInDifferentCase_thenThrowsConflictException() {
        userRepository.createUser(user);

        User duplicateUser = new User();
        duplicateUser.setName("Petr");
        duplicateUser.setEmail("IVAN@MAIL.COM");

        assertThrows(ConflictException.class, () -> userRepository.createUser(duplicateUser));
    }

    @Test
    void getAllUsers_whenUsersExist_thenReturnsCorrectList() {
        userRepository.createUser(user);

        User secondUser = new User();
        secondUser.setName("Petr");
        secondUser.setEmail("petr@mail.com");
        userRepository.createUser(secondUser);

        List<User> users = userRepository.getAllUsers();

        assertEquals(2, users.size());
    }

    @Test
    void getUserById_whenUserExists_thenReturnsOptionalWithUser() {
        User savedUser = userRepository.createUser(user);

        Optional<User> foundUserOpt = userRepository.getUserById(savedUser.getId());

        assertTrue(foundUserOpt.isPresent());
        assertEquals(savedUser.getId(), foundUserOpt.get().getId());
    }

    @Test
    void getUserById_whenUserDoesNotExist_thenReturnsEmptyOptional() {
        Optional<User> foundUserOpt = userRepository.getUserById(99L);

        assertTrue(foundUserOpt.isEmpty());
    }

    @Test
    void updateUser_whenEmailIsUnique_thenSuccess() {
        User savedUser = userRepository.createUser(user);
        savedUser.setName("Ivan Updated");
        savedUser.setEmail("ivan_new@mail.com");

        User updatedUser = userRepository.updateUser(savedUser);

        assertEquals("Ivan Updated", updatedUser.getName());
        assertEquals("ivan_new@mail.com", updatedUser.getEmail());
    }

    @Test
    void updateUser_whenEmailBelongsToOtherUser_thenThrowsConflictException() {
        User user1 = userRepository.createUser(user);

        User user2 = new User();
        user2.setName("Petr");
        user2.setEmail("petr@mail.com");
        userRepository.createUser(user2);

        user2.setEmail("ivan@mail.com");

        assertThrows(ConflictException.class, () -> userRepository.updateUser(user2));
    }

    @Test
    void updateUser_whenEmailBelongsToSameUser_thenSuccess() {
        User user1 = userRepository.createUser(user);

        user1.setName("Ivan1");

        assertDoesNotThrow(() -> userRepository.updateUser(user1));
        assertEquals("Ivan1", userRepository.getUserById(user1.getId()).get().getName());
    }

    @Test
    void deleteUser_whenUserExists_thenRemovesUser() {
        User savedUser = userRepository.createUser(user);
        Long id = savedUser.getId();

        userRepository.deleteUser(id);

        Optional<User> foundUserOpt = userRepository.getUserById(id);
        assertTrue(foundUserOpt.isEmpty());
    }
}
