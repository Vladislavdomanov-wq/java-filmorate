package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private final Map<Long, Set<Long>> likes = new HashMap<>();

    @Override
    public Film findById(Long id) {
        return films.getOrDefault(id, null);
    }

    @Override
    public Film create(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Создан новый фильм: {}", film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        Film existingFilm = films.get(newFilm.getId());

        if (existingFilm == null) {  // ← Просто проверяем, получили ли мы что-то
            log.warn("Фильм с id = " + newFilm.getId() + " не найден");
            return null;
        }

        if (newFilm.getName() != null) existingFilm.setName(newFilm.getName());
        if (newFilm.getDescription() != null) existingFilm.setDescription(newFilm.getDescription());
        if (newFilm.getReleaseDate() != null) existingFilm.setReleaseDate(newFilm.getReleaseDate());
        if (newFilm.getDuration() != null) existingFilm.setDuration(newFilm.getDuration());
        log.info("Обновлён фильм: {}", existingFilm);
        return existingFilm;
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        likes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        log.info("Пользователь {} лайкнул фильм {}", userId, filmId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        Set<Long> filmLikes = likes.get(filmId);
        if (filmLikes != null) {
            filmLikes.remove(userId);
        }
        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        return films.values().stream()
                .sorted((f1, f2) -> {
                    int likes1 = likes.getOrDefault(f1.getId(), new HashSet<>()).size();
                    int likes2 = likes.getOrDefault(f2.getId(), new HashSet<>()).size();
                    return Integer.compare(likes2, likes1);
                })
                .limit(count)
                .toList();
    }

    private long getNextId() {
        return films.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0) + 1;
    }
}
