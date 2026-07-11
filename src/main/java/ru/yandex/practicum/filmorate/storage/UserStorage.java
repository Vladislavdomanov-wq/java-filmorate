package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User findById(Long id);
    User create(User user);
    User update(User newUser);
    Collection<User> findAll();
}
