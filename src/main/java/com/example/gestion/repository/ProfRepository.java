package com.example.gestion.repository;

import com.example.gestion.model.Prof;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class ProfRepository {

    private static final String SQL_FIND_ALL =
            "SELECT codeprof, nom, prenom, grade FROM prof ORDER BY codeprof";
    private static final String SQL_FIND_BY_ID =
            "SELECT codeprof, nom, prenom, grade FROM prof WHERE codeprof = ?";
    private static final String SQL_FIND_BY_NOM =
            "SELECT codeprof, nom, prenom, grade FROM prof WHERE nom ILIKE ? ORDER BY codeprof";
    private static final String SQL_FIND_BY_NOM_EXACT =
            "SELECT codeprof, nom, prenom, grade FROM prof WHERE lower(btrim(nom)) = lower(?)";
    private static final String SQL_FIND_BY_NOM_PRENOM =
            "SELECT codeprof, nom, prenom, grade FROM prof "
                    + "WHERE lower(btrim(nom)) = lower(?) AND lower(btrim(prenom)) = lower(?)";
    private static final String SQL_INSERT_AUTO =
            "INSERT INTO prof (nom, prenom, grade) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE =
            "UPDATE prof SET nom = ?, prenom = ?, grade = ? WHERE codeprof = ?";
    private static final String SQL_DELETE =
            "DELETE FROM prof WHERE codeprof = ?";
    private static final String SQL_EXISTS =
            "SELECT COUNT(*) FROM prof WHERE codeprof = ?";

    private static final RowMapper<Prof> MAPPER = (rs, rowNum) -> new Prof(
            rs.getInt("codeprof"),
            rs.getString("nom"),
            rs.getString("prenom"),
            rs.getString("grade"));

    private final JdbcTemplate jdbcTemplate;

    public ProfRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Prof> findAll() {
        return jdbcTemplate.query(SQL_FIND_ALL, MAPPER);
    }

    public Optional<Prof> findById(Integer codeprof) {
        return jdbcTemplate.query(SQL_FIND_BY_ID, MAPPER, codeprof).stream().findFirst();
    }

    public List<Prof> findByNom(String nom) {
        return jdbcTemplate.query(SQL_FIND_BY_NOM, MAPPER, "%" + nom + "%");
    }

    public List<Prof> findByNomExact(String nom) {
        return jdbcTemplate.query(SQL_FIND_BY_NOM_EXACT, MAPPER, nom);
    }

    public List<Prof> findByNomPrenom(String nom, String prenom) {
        return jdbcTemplate.query(SQL_FIND_BY_NOM_PRENOM, MAPPER, nom, prenom);
    }

    public int insert(Prof prof) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT_AUTO, new String[]{"codeprof"});
            ps.setString(1, prof.getNom());
            ps.setString(2, prof.getPrenom());
            ps.setString(3, prof.getGrade());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? -1 : key.intValue();
    }

    public void update(Prof prof) {
        jdbcTemplate.update(SQL_UPDATE, prof.getNom(), prof.getPrenom(), prof.getGrade(), prof.getCodeprof());
    }

    public void deleteById(Integer codeprof) {
        jdbcTemplate.update(SQL_DELETE, codeprof);
    }

    public boolean existsById(Integer codeprof) {
        Integer count = jdbcTemplate.queryForObject(SQL_EXISTS, Integer.class, codeprof);
        return count != null && count > 0;
    }
}
