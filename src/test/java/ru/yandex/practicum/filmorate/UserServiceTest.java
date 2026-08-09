package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(new InMemoryUserStorage());
    }

    @Test
    void create_validUser_success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userService.create(user);

        assertNotNull(created.getId());
        assertEquals("test@example.com", created.getEmail());
        assertEquals("testuser", created.getLogin());
    }

    @Test
    void create_invalidEmail_throwsException() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("testuser");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(ValidationException.class, () -> userService.create(user));
    }

    @Test
    void create_emptyLogin_throwsException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("   ");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(ValidationException.class, () -> userService.create(user));
    }

    @Test
    void create_futureBirthday_throwsException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userService.create(user));
    }

    @Test
    void create_emptyName_setsLoginAsName() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userService.create(user);

        assertEquals("testuser", created.getName());
    }

    @Test
    void findById_existingUser_success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userService.create(user);
        User found = userService.findById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("test@example.com", found.getEmail());
    }

    @Test
    void findById_nonExistingUser_throwsException() {
        assertThrows(NotFoundException.class, () -> userService.findById(999L));
    }

    @Test
    void findAll_returnsAllUsers() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1995, 5, 15));

        userService.create(user1);
        userService.create(user2);

        Collection<User> users = userService.findAll();

        assertEquals(2, users.size());
    }

    @Test
    void addFriend_success() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1995, 5, 15));

        User created1 = userService.create(user1);
        User created2 = userService.create(user2);

        userService.addFriend(created1.getId(), created2.getId());

        Collection<User> friends = userService.getFriends(created1.getId());

        assertEquals(1, friends.size());
        assertEquals(created2.getId(), friends.iterator().next().getId());
    }

    @Test
    void removeFriend_success() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1995, 5, 15));

        User created1 = userService.create(user1);
        User created2 = userService.create(user2);

        userService.addFriend(created1.getId(), created2.getId());
        userService.removeFriend(created1.getId(), created2.getId());

        Collection<User> friends = userService.getFriends(created1.getId());

        assertEquals(0, friends.size());
    }
}