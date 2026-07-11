package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Film findById(Long id);
    Film create(Film film);
    Film update(Film newFilm);
    Collection<Film> findAll();
}
