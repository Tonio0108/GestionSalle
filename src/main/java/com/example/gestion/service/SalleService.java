package com.example.gestion.service;

import com.example.gestion.model.Salle;
import com.example.gestion.repository.SalleRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalleService {

    private final SalleRepository salleRepository;

    public SalleService(SalleRepository salleRepository) {
        this.salleRepository = salleRepository;
    }

    public List<Salle> lister() {
        return salleRepository.findAll();
    }

    public Salle trouverParCode(Integer codesal) {
        if (codesal == null || codesal <= 0) {
            throw new IllegalArgumentException("Le code salle doit être un entier positif.");
        }
        return salleRepository.findById(codesal).orElse(null);
    }

    public Salle ajouter(Salle salle) {
        valider(salle);
        verifierUnicite(salle.getDesignation(), null);
        int codeGenere = salleRepository.insert(salle);
        salle.setCodesal(codeGenere);
        return salle;
    }

    public void modifier(Salle salle) {
        valider(salle);
        if (!salleRepository.existsById(salle.getCodesal())) {
            throw new IllegalArgumentException("Salle introuvable (code " + salle.getCodesal() + ").");
        }
        verifierUnicite(salle.getDesignation(), salle.getCodesal());
        salleRepository.update(salle);
    }

    public void supprimer(Integer codesal) {
        try {
            salleRepository.deleteById(codesal);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "Impossible de supprimer cette salle : des occupations y sont associées. "
                            + "Supprimez d'abord ces occupations.");
        }
    }

    private void valider(Salle salle) {
        if (salle == null) {
            throw new IllegalArgumentException("Les données de la salle sont manquantes.");
        }
        String designation = salle.getDesignation() == null ? "" : salle.getDesignation().trim();
        if (designation.isEmpty()) {
            throw new IllegalArgumentException("La désignation est obligatoire.");
        }
        if (designation.length() < 2 || designation.length() > 150) {
            throw new IllegalArgumentException("La désignation doit contenir entre 2 et 150 caractères.");
        }
        salle.setDesignation(designation);
    }

    private void verifierUnicite(String designation, Integer codeExclu) {
        boolean existe = salleRepository.findByDesignationExact(designation).stream()
                .anyMatch(s -> !s.getCodesal().equals(codeExclu));
        if (existe) {
            throw new IllegalStateException("Une salle « " + designation + " » existe déjà.");
        }
    }
}
