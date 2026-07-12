package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {
    private FilmService filmService;
    private UserService userService;  // ← добавь

    @BeforeEach
    void setUp() {
        userService = new UserService(new InMemoryUserStorage());  // ← создай UserService
        filmService = new FilmService(new InMemoryFilmStorage(), userService);  // ← передай оба параметра
    }

    @Test
    void create_validFilm_success() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film created = filmService.create(film);

        assertNotNull(created.getId());
        assertEquals("Test Film", created.getName());
    }

    @Test
    void create_emptyName_throwsException() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmService.create(film));
    }

    @Test
    void create_longDescription_throwsException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmService.create(film));
    }

    @Test
    void create_oldReleaseDate_throwsException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(1890, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmService.create(film));
    }

    @Test
    void create_negativeDuration_throwsException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-10);

        assertThrows(ValidationException.class, () -> filmService.create(film));
    }

    @Test
    void addLike_success() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film created = filmService.create(film);

        assertDoesNotThrow(() -> filmService.addLike(created.getId(), 1L));
    }

    @Test
    void getPopularFilms_returnsSortedByLikes() {
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(130);

        Film created1 = filmService.create(film1);
        Film created2 = filmService.create(film2);

        filmService.addLike(created1.getId(), 1L);
        filmService.addLike(created2.getId(), 1L);
        filmService.addLike(created2.getId(), 2L);

        Collection<Film> popular = filmService.getPopularFilms(10);

        assertEquals(2, popular.size());
        Film[] filmsArray = popular.toArray(new Film[0]);
        assertEquals(created2.getId(), filmsArray[0].getId());
    }
}