package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;

    @Override
    public Film findById(Long id) {
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f " +
                "JOIN mpa_ratings m ON m.id = f.mpa_id WHERE f.id = ?";
        try {
            Film film = jdbc.queryForObject(sql, this::mapFilm, id);
            loadGenres(film);
            return film;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Film create(Film film) {
        checkMpa(film);
        validateGenres(film);
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        Number generatedId = keyHolder.getKey();
        if (generatedId != null) {
            film.setId(generatedId.longValue());
        }
        saveGenres(film);
        fillMpaName(film);
        fillGenreNamesInOrder(film);
        log.info("Создан фильм в БД: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (findById(film.getId()) == null) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        checkMpa(film);
        validateGenres(film);
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_id = ? WHERE id = ?";
        jdbc.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());
        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(film);
        log.info("Обновлён фильм в БД: {}", film);
        return findById(film.getId());
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f " +
                "JOIN mpa_ratings m ON m.id = f.mpa_id";
        List<Film> films = jdbc.query(sql, this::mapFilm);
        films.forEach(this::loadGenres);
        return films;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbc.update("MERGE INTO likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)",
                filmId, userId);
        log.info("Пользователь {} лайкнул фильм {}", userId, filmId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbc.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                "f.mpa_id, m.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa_ratings m ON m.id = f.mpa_id " +
                "LEFT JOIN likes l ON l.film_id = f.id " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name " +
                "ORDER BY COUNT(l.user_id) DESC LIMIT ?";
        List<Film> films = jdbc.query(sql, this::mapFilm, count);
        films.forEach(this::loadGenres);
        return films;
    }

    private void checkMpa(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new ValidationException("У фильма должен быть рейтинг MPA");
        }
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM mpa_ratings WHERE id = ?",
                Integer.class, film.getMpa().getId());
        if (count == 0) {
            throw new NotFoundException("Рейтинг с id = " + film.getMpa().getId() + " не найден");
        }
    }

    private void validateGenres(Film film) {
        List<Long> ids = genreIds(film);
        if (ids.isEmpty()) {
            return;
        }
        List<Long> found = jdbc.queryForList(
                "SELECT id FROM genres WHERE id IN (" + placeholders(ids.size()) + ")",
                Long.class, ids.toArray());
        if (found.size() != ids.size()) {
            throw new NotFoundException("Один из указанных жанров не найден");
        }
    }

    private void saveGenres(Film film) {
        List<Long> ids = genreIds(film);
        if (ids.isEmpty()) {
            return;
        }
        StringBuilder values = new StringBuilder();
        List<Object> params = new ArrayList<>();
        for (Long genreId : ids) {
            values.append("(?, ?),");
            params.add(film.getId());
            params.add(genreId);
        }
        values.setLength(values.length() - 1);
        jdbc.update("MERGE INTO film_genres (film_id, genre_id) KEY (film_id, genre_id) VALUES " + values,
                params.toArray());
    }

    private void fillGenreNamesInOrder(Film film) {
        List<Long> ids = genreIds(film);
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, String> namesById = jdbc.queryForList(
                        "SELECT id, name FROM genres WHERE id IN (" + placeholders(ids.size()) + ")",
                        ids.toArray())
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("id")).longValue(),
                        row -> (String) row.get("name")));
        Set<Genre> filled = new LinkedHashSet<>();
        for (Genre genre : film.getGenres()) {
            if (genre != null && genre.getId() != null) {
                filled.add(new Genre(genre.getId(), namesById.get(genre.getId())));
            }
        }
        film.setGenres(filled);
    }

    private List<Long> genreIds(Film film) {
        if (film.getGenres() == null) {
            return List.of();
        }
        return film.getGenres().stream()
                .filter(g -> g != null && g.getId() != null)
                .map(Genre::getId)
                .distinct()
                .toList();
    }

    private String placeholders(int count) {
        return String.join(", ", Collections.nCopies(count, "?"));
    }

    private void fillMpaName(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            film.setMpa(jdbc.queryForObject("SELECT id, name FROM mpa_ratings WHERE id = ?",
                    (rs, rowNum) -> new Mpa(rs.getLong("id"), rs.getString("name")),
                    film.getMpa().getId()));
        }
    }

    private void loadGenres(Film film) {
        List<Genre> genres = jdbc.query(
                "SELECT g.id, g.name FROM genres g " +
                        "JOIN film_genres fg ON fg.genre_id = g.id " +
                        "WHERE fg.film_id = ? ORDER BY g.id",
                (rs, rowNum) -> new Genre(rs.getLong("id"), rs.getString("name")),
                film.getId());
        film.setGenres(new LinkedHashSet<>(genres));
    }

    private Film mapFilm(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setMpa(new Mpa(rs.getLong("mpa_id"), rs.getString("mpa_name")));
        return film;
    }
}