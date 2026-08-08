package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

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
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, (SELECT id FROM mpa_ratings WHERE name = ?))";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setString(5, mpaToDbName(film.getMpa()));
            return ps;
        }, keyHolder);
        Number generatedId = keyHolder.getKey();
        if (generatedId != null) {
            film.setId(generatedId.longValue());
        }
        saveGenres(film);
        log.info("Создан фильм в БД: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (findById(film.getId()) == null) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_id = (SELECT id FROM mpa_ratings WHERE name = ?) WHERE id = ?";
        jdbc.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), mpaToDbName(film.getMpa()), film.getId());
        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(film);
        log.info("Обновлён фильм в БД: {}", film);
        return film;
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
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, m.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa_ratings m ON m.id = f.mpa_id " +
                "LEFT JOIN likes l ON l.film_id = f.id " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, m.name " +
                "ORDER BY COUNT(l.user_id) DESC LIMIT ?";
        List<Film> films = jdbc.query(sql, this::mapFilm, count);
        films.forEach(this::loadGenres);
        return films;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null) {
            return;
        }
        for (String genreName : film.getGenres()) {
            jdbc.update("MERGE INTO genres (name) KEY (name) VALUES (?)", genreName);
            jdbc.update("MERGE INTO film_genres (film_id, genre_id) KEY (film_id, genre_id) " +
                    "VALUES (?, (SELECT id FROM genres WHERE name = ?))", film.getId(), genreName);
        }
    }

    private void loadGenres(Film film) {
        List<String> names = jdbc.queryForList(
                "SELECT g.name FROM genres g " +
                        "JOIN film_genres fg ON fg.genre_id = g.id WHERE fg.film_id = ?",
                String.class, film.getId());
        film.setGenres(new HashSet<>(names));
    }

    private Film mapFilm(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setMpa(mpaFromDbName(rs.getString("mpa_name")));
        return film;
    }

    private String mpaToDbName(MpaRating mpa) {
        return mpa.name().replace('_', '-');
    }

    private MpaRating mpaFromDbName(String name) {
        return MpaRating.valueOf(name.replace('-', '_'));
    }
}