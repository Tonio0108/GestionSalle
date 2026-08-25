package com.example.gestion.repository;

import com.example.gestion.model.Occuper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public class OccuperRepository {

    private static final String SQL_FIND_ALL = """
            SELECT o.codeprof, o.codesal, o.date AS date_occ, o.heure AS heure_occ,
                   p.nom || ' ' || p.prenom AS nom_prof,
                   s.designation AS designation_salle
            FROM occuper o
            JOIN prof p ON p.codeprof = o.codeprof
            JOIN salle s ON s.codesal = o.codesal
            ORDER BY o.date DESC, o.heure, p.nom, p.prenom""";

    private static final String SQL_INSERT =
            "INSERT INTO occuper (codeprof, codesal, date, heure) VALUES (?, ?, ?, ?)";

    private static final String SQL_UPDATE = """
            UPDATE occuper
            SET codeprof = ?, codesal = ?, date = ?, heure = ?
            WHERE codeprof = ? AND codesal = ? AND date = ? AND heure = ?""";

    private static final String SQL_DELETE =
            "DELETE FROM occuper WHERE codeprof = ? AND codesal = ? AND date = ? AND heure = ?";

    private static final RowMapper<Occuper> MAPPER = (rs, rowNum) -> new Occuper(
            rs.getInt("codeprof"),
            rs.getInt("codesal"),
            rs.getDate("date_occ").toLocalDate(),
            rs.getTime("heure_occ").toLocalTime(),
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
                Date.valueOf(occuper.getDate()),
                Time.valueOf(occuper.getHeure()));
    }

    public void update(Integer ancienCodeprof, Integer ancienCodesal,
                       LocalDate ancienneDate, LocalTime ancienneHeure, Occuper nouveau) {
        jdbcTemplate.update(SQL_UPDATE,
                nouveau.getCodeprof(),
                nouveau.getCodesal(),
                Date.valueOf(nouveau.getDate()),
                Time.valueOf(nouveau.getHeure()),
                ancienCodeprof,
                ancienCodesal,
                Date.valueOf(ancienneDate),
                Time.valueOf(ancienneHeure));
    }

    public void delete(Integer codeprof, Integer codesal, LocalDate date, LocalTime heure) {
        jdbcTemplate.update(SQL_DELETE, codeprof, codesal, Date.valueOf(date), Time.valueOf(heure));
    }
}
