package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    private User createUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Имя");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return userStorage.create(user);
    }

    @Test
    void createAndFindById() {
        User created = createUser("a@a.ru", "loginA");
        assertThat(created.getId()).isNotNull();

        User found = userStorage.findById(created.getId());
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("a@a.ru");
    }

    @Test
    void findAll() {
        createUser("b@b.ru", "loginB");
        Collection<User> users = userStorage.findAll();
        assertThat(users).isNotEmpty();
    }

    @Test
    void update() {
        User created = createUser("c@c.ru", "loginC");
        created.setName("Новое имя");
        User updated = userStorage.update(created);
        assertThat(updated.getName()).isEqualTo("Новое имя");
    }

    @Test
    void friendsAndCommonFriends() {
        User a = createUser("d@d.ru", "loginD");
        User b = createUser("e@e.ru", "loginE");
        User c = createUser("f@f.ru", "loginF");

        userStorage.addFriend(a.getId(), b.getId());
        userStorage.addFriend(a.getId(), c.getId());
        userStorage.addFriend(b.getId(), c.getId());

        assertThat(userStorage.getFriends(a.getId())).hasSize(2);
        assertThat(userStorage.getCommonFriends(a.getId(), b.getId())).hasSize(1);

        userStorage.removeFriend(a.getId(), b.getId());
        assertThat(userStorage.getFriends(a.getId())).hasSize(1);
    }
}