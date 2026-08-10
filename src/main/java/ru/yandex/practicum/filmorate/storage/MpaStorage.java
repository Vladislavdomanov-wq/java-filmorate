package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MpaStorage {
    private final JdbcTemplate jdbc;

    public List<Mpa> findAll() {
        return jdbc.query("SELECT * FROM mpa_ratings ORDER BY id",
                (rs, rowNum) -> new Mpa(rs.getLong("id"), rs.getString("name")));
    }

    public Mpa findById(Long id) {
        try {
            return jdbc.queryForObject("SELECT * FROM mpa_ratings WHERE id = ?",
                    (rs, rowNum) -> new Mpa(rs.getLong("id"), rs.getString("name")), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}