package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private UserDbStorage userStorage;

    private User createUser() {
        User user = new User();
        user.setEmail("like@test.ru");
        user.setLogin("likelogin");
        user.setName("Лайкер");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return userStorage.create(user);
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        film.setMpa(new Mpa(1L, null));
        film.setGenres(Set.of(new Genre(1L, null), new Genre(2L, null)));
        return filmStorage.create(film);
    }

    @Test
    void createAndFindById() {
        Film created = createFilm("Фильм А");
        assertThat(created.getId()).isNotNull();

        Film found = filmStorage.findById(created.getId());
        assertThat(found.getName()).isEqualTo("Фильм А");
        assertThat(found.getMpa().getName()).isEqualTo("G");
        assertThat(found.getGenres()).hasSize(2);
    }

    @Test
    void update() {
        Film created = createFilm("Фильм Б");
        created.setName("Фильм Б обновлённый");
        Film updated = filmStorage.update(created);
        assertThat(updated.getName()).isEqualTo("Фильм Б обновлённый");
    }

    @Test
    void findAll() {
        createFilm("Фильм В");
        Collection<Film> films = filmStorage.findAll();
        assertThat(films).isNotEmpty();
    }

    @Test
    void likesAndPopular() {
        User user = createUser();
        Film film1 = createFilm("Фильм Г");
        createFilm("Фильм Д");

        filmStorage.addLike(film1.getId(), user.getId());

        Collection<Film> popular = filmStorage.getPopularFilms(10);
        assertThat(popular).isNotEmpty();
        assertThat(popular.iterator().next().getId()).isEqualTo(film1.getId());

        filmStorage.removeLike(film1.getId(), user.getId());
    }
}