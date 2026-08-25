package com.example.gestion.service;

import com.example.gestion.model.Occuper;
import com.example.gestion.repository.OccuperRepository;
import com.example.gestion.repository.ProfRepository;
import com.example.gestion.repository.SalleRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OccuperService {

    private static final LocalDate DATE_MIN = LocalDate.of(2010, 1, 1);
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMAT_HEURE = DateTimeFormatter.ofPattern("HH:mm");

    private final OccuperRepository occuperRepository;
    private final ProfRepository profRepository;
    private final SalleRepository salleRepository;

    public OccuperService(OccuperRepository occuperRepository,
                          ProfRepository profRepository,
                          SalleRepository salleRepository) {
        this.occuperRepository = occuperRepository;
        this.profRepository = profRepository;
        this.salleRepository = salleRepository;
    }

    public List<Occuper> lister() {
        return occuperRepository.findAll();
    }

    public void enregistrer(Occuper occuper) {
        verifier(occuper);
        try {
            occuperRepository.insert(occuper);
        } catch (DuplicateKeyException e) {
            throw traduireDoublon(e);
        }
    }

    public void modifier(Integer ancienCodeprof, Integer ancienCodesal,
                         LocalDate ancienneDate, LocalTime ancienneHeure, Occuper nouveau) {
        verifier(nouveau);
        try {
            occuperRepository.update(ancienCodeprof, ancienCodesal, ancienneDate, ancienneHeure, nouveau);
        } catch (DuplicateKeyException e) {
            throw traduireDoublon(e);
        }
    }

    public void supprimer(Integer codeprof, Integer codesal, LocalDate date, LocalTime heure) {
        occuperRepository.delete(codeprof, codesal, date, heure);
    }

    private void verifier(Occuper occuper) {
        if (occuper == null || occuper.getCodeprof() == null
                || occuper.getCodesal() == null || occuper.getDate() == null
                || occuper.getHeure() == null) {
            throw new IllegalArgumentException("Le professeur, la salle, la date et l'heure sont obligatoires.");
        }
        LocalDate dateMax = LocalDate.now().plusMonths(6);
        if (occuper.getDate().isBefore(DATE_MIN) || occuper.getDate().isAfter(dateMax)) {
            throw new IllegalArgumentException("La date doit être comprise entre le "
                    + DATE_MIN.format(FORMAT) + " et le " + dateMax.format(FORMAT) + ".");
        }
        if (!profRepository.existsById(occuper.getCodeprof())) {
            throw new IllegalArgumentException("Professeur introuvable (code " + occuper.getCodeprof() + ").");
        }
        if (!salleRepository.existsById(occuper.getCodesal())) {
            throw new IllegalArgumentException("Salle introuvable (code " + occuper.getCodesal() + ").");
        }
    }

    private IllegalStateException traduireDoublon(DuplicateKeyException e) {
        String message = e.getMessage() == null ? "" : e.getMessage();
        if (message.contains("ux_occuper_salle_date_heure")) {
            return new IllegalStateException(
                    "Cette salle est déjà occupée à cette date et à cette heure.");
        }
        if (message.contains("occuper_pkey")) {
            return new IllegalStateException(
                    "Ce professeur occupe déjà cette salle à cette date et à cette heure.");
        }
        return new IllegalStateException(
                "Cette occupation existe déjà : mêmes professeur, salle, date et heure.");
    }
}
