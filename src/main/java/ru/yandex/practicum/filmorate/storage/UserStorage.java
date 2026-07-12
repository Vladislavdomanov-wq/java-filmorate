package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User findById(Long id);
    User create(User user);
    User update(User newUser);
    Collection<User> findAll();
    void addFriend(Long userId, Long friendId);
    void removeFriend(Long userId, Long friendId);
    Collection<User> getFriends(Long userId);
    Collection<User> getCommonFriends(Long userId, Long otherId);
}
