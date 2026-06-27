package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    @Test
    void createFilm_shouldThrowWhenNameIsEmpty() {
        FilmController controller = new FilmController();
        Film film = new Film();
        film.setName("");
        film.setDuration(120);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void createFilm_shouldThrowWhenReleaseDateTooEarly() {
        FilmController controller = new FilmController();
        Film film = new Film();
        film.setName("Фильм");
        film.setDuration(120);
        film.setReleaseDate(LocalDate.of(1800, 1, 1));

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void createFilm_shouldThrowWhenDurationNegative() {
        FilmController controller = new FilmController();
        Film film = new Film();
        film.setName("Фильм");
        film.setDuration(-10);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void createFilm_shouldReturnFilmWithId() {
        FilmController controller = new FilmController();
        Film film = new Film();
        film.setName("Фильм");
        film.setDuration(120);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));

        Film created = controller.create(film);
        assertNotNull(created.getId());
        assertEquals("Фильм", created.getName());
    }
}