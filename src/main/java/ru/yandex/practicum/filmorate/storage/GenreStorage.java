package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GenreStorage {
    private final JdbcTemplate jdbc;

    public List<Genre> findAll() {
        return jdbc.query("SELECT * FROM genres ORDER BY id",
                (rs, rowNum) -> new Genre(rs.getLong("id"), rs.getString("name")));
    }

    public Genre findById(Long id) {
        try {
            return jdbc.queryForObject("SELECT * FROM genres WHERE id = ?",
                    (rs, rowNum) -> new Genre(rs.getLong("id"), rs.getString("name")), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}