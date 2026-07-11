package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

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
