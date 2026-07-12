package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, Set<Long>> friends = new HashMap<>();

    @Override
    public void addFriend(Long userId, Long friendId) {

        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);

        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);

        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {

        Set<Long> userFriends = friends.get(userId);
        if (userFriends != null) {
            userFriends.remove(friendId);
        }

        Set<Long> friendFriends = friends.get(friendId);
        if(friendFriends != null) {
            friendFriends.remove(userId);
        }
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        Set<Long> friendIds = friends.get(userId);

        if (friendIds == null) {
            return new ArrayList<>();
        }

        List<User> friendsList = new ArrayList<>();
        for (Long friendId : friendIds) {
            User friend = findById(friendId);
            if (friend != null) {
                friendsList.add(friend);
            }
        }

        return friendsList;
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        Set<Long> friendsOfUser = friends.getOrDefault(userId, new HashSet<>());
        Set<Long> friendsOfOther = friends.getOrDefault(otherId, new HashSet<>());

        // Находим пересечение (копируем, чтобы не менять оригинал)
        Set<Long> commonIds = new HashSet<>(friendsOfUser);
        commonIds.retainAll(friendsOfOther);  // Оставляем только общие элементы

        // Ищем каждого общего друга по ID
        List<User> commonFriends = new ArrayList<>();
        for (Long id : commonIds) {
            User friend = findById(id);
            if (friend != null) {
                commonFriends.add(friend);
            }
        }

        return commonFriends;
    }

    private final Map<Long, User> users = new HashMap<>();

    private long getNextId() {
        return users.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0) + 1;
    }

    @Override
    public User findById(Long id) {
        return users.getOrDefault(id, null);
    }

    @Override
    public User create(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Создан новый пользователь: {}", user);
        return user;
    }

    @Override
    public User update(User newUser) {
        User existingUser = users.get(newUser.getId());

        if (existingUser == null) {
            log.warn("Пользователь с id = {} не найден", newUser.getId());
            return null;
        }

        if (newUser.getEmail() != null) existingUser.setEmail(newUser.getEmail());
        if (newUser.getLogin() != null) existingUser.setLogin(newUser.getLogin());
        if (newUser.getName() != null) existingUser.setName(newUser.getName());
        if (newUser.getBirthday() != null) existingUser.setBirthday(newUser.getBirthday());

        log.info("Обновлён пользователь: {} ", existingUser);
        return existingUser;
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }
}
