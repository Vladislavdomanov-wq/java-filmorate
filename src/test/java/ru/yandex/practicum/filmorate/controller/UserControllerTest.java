package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserControllerTest {
    @Test
    void createUser_shouldThrowWhenEmailWithoutAt() {
        UserController controller = new UserController();
        User user = new User();
        user.setName("flagman");
        user.setLogin("111111");
        user.setEmail("mlgtrackyandex.ru");
        user.setBirthday(LocalDate.of(2027, 8, 20));
        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void createUser_shouldThrowWhenLoginEmpty() {
        UserController controller = new UserController();
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void createUser_shouldThrowWhenBirthdayInFuture() {
        UserController controller = new UserController();
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> controller.create(user));
    }
}
