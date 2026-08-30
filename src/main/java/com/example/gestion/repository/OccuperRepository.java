package com.example.gestion.repository;

import com.example.gestion.model.Occuper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Repository
public class OccuperRepository {

    private static final String SQL_FIND_ALL = """
            SELECT o.codeprof, o.codesal, o.date AS date_occ,
                   p.nom || ' ' || p.prenom AS nom_prof,
                   s.designation AS designation_salle
            FROM occuper o
            JOIN prof p ON p.codeprof = o.codeprof
            JOIN salle s ON s.codesal = o.codesal
            ORDER BY o.date DESC, p.nom, p.prenom""";

    private static final String SQL_INSERT =
            "INSERT INTO occuper (codeprof, codesal, date) VALUES (?, ?, ?)";

    private static final String SQL_UPDATE = """
            UPDATE occuper
            SET codeprof = ?, codesal = ?, date = ?
            WHERE codeprof = ? AND codesal = ? AND date = ?""";

    private static final String SQL_DELETE =
            "DELETE FROM occuper WHERE codeprof = ? AND codesal = ? AND date = ?";

    private static final RowMapper<Occuper> MAPPER = (rs, rowNum) -> new Occuper(
            rs.getInt("codeprof"),
            rs.getInt("codesal"),
            rs.getDate("date_occ").toLocalDate(),
            rs.getString("nom_prof"),
            rs.getString("designation_salle"));

    private final JdbcTemplate jdbcTemplate;

    public OccuperRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Occuper> findAll() {
        return jdbcTemplate.query(SQL_FIND_ALL, MAPPER);
    }

    public void insert(Occuper occuper) {
        jdbcTemplate.update(SQL_INSERT,
                occuper.getCodeprof(),
                occuper.getCodesal(),
                Date.valueOf(occuper.getDate()));
    }

    public void update(Integer ancienCodeprof, Integer ancienCodesal,
                       LocalDate ancienneDate, Occuper nouveau) {
        jdbcTemplate.update(SQL_UPDATE,
                nouveau.getCodeprof(),
                nouveau.getCodesal(),
                Date.valueOf(nouveau.getDate()),
                ancienCodeprof,
                ancienCodesal,
                Date.valueOf(ancienneDate));
    }

    public void delete(Integer codeprof, Integer codesal, LocalDate date) {
        jdbcTemplate.update(SQL_DELETE, codeprof, codesal, Date.valueOf(date));
    }
}
