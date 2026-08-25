package com.example.gestion.repository;

import com.example.gestion.model.Salle;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class SalleRepository {

    private static final String SQL_FIND_ALL =
            "SELECT codesal, designation FROM salle ORDER BY codesal";
    private static final String SQL_FIND_BY_ID =
            "SELECT codesal, designation FROM salle WHERE codesal = ?";
    private static final String SQL_FIND_BY_DESIGNATION_EXACT =
            "SELECT codesal, designation FROM salle WHERE upper(btrim(designation)) = upper(?)";
    private static final String SQL_INSERT_AUTO =
            "INSERT INTO salle (designation) VALUES (?)";
    private static final String SQL_UPDATE =
            "UPDATE salle SET designation = ? WHERE codesal = ?";
    private static final String SQL_DELETE =
            "DELETE FROM salle WHERE codesal = ?";
    private static final String SQL_EXISTS =
            "SELECT COUNT(*) FROM salle WHERE codesal = ?";

    private static final RowMapper<Salle> MAPPER = (rs, rowNum) -> new Salle(
            rs.getInt("codesal"),
            rs.getString("designation"));

    private final JdbcTemplate jdbcTemplate;

    public SalleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Salle> findAll() {
        return jdbcTemplate.query(SQL_FIND_ALL, MAPPER);
    }

    public Optional<Salle> findById(Integer codesal) {
        return jdbcTemplate.query(SQL_FIND_BY_ID, MAPPER, codesal).stream().findFirst();
    }

    public List<Salle> findByDesignationExact(String designation) {
        return jdbcTemplate.query(SQL_FIND_BY_DESIGNATION_EXACT, MAPPER, designation);
    }

    public int insert(Salle salle) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT_AUTO, new String[]{"codesal"});
            ps.setString(1, salle.getDesignation());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? -1 : key.intValue();
    }

    public void update(Salle salle) {
        jdbcTemplate.update(SQL_UPDATE, salle.getDesignation(), salle.getCodesal());
    }

    public void deleteById(Integer codesal) {
        jdbcTemplate.update(SQL_DELETE, codesal);
    }

    public boolean existsById(Integer codesal) {
        Integer count = jdbcTemplate.queryForObject(SQL_EXISTS, Integer.class, codesal);
        return count != null && count > 0;
    }
}
