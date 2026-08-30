package com.example.gestion.service;

import com.example.gestion.model.Prof;
import com.example.gestion.repository.ProfRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class ProfService {

    public static final List<String> GRADES = List.of(
            "Vacataire", "Assistant", "Maître de conférences", "Professeur");

    private static final Pattern NOM_PATTERN = Pattern.compile("[\\p{L}' -]{2,100}");

    private final ProfRepository profRepository;

    public ProfService(ProfRepository profRepository) {
        this.profRepository = profRepository;
    }

    public List<Prof> lister() {
        return profRepository.findAll();
    }

    public Prof trouverParCode(Integer codeprof) {
        if (codeprof == null || codeprof <= 0) {
            throw new IllegalArgumentException("Le code professeur doit être un entier positif.");
        }
        return profRepository.findById(codeprof).orElse(null);
    }

    public List<Prof> chercherParNom(String nom) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Veuillez saisir un nom à rechercher.");
        }
        return profRepository.findByNom(nom.trim());
    }

    public Prof ajouter(Prof prof) {
        valider(prof);
        verifierUnicite(prof.getNom(), prof.getPrenom(), null);
        int codeGenere = profRepository.insert(prof);
        prof.setCodeprof(codeGenere);
        return prof;
    }

    public void modifier(Prof prof) {
        valider(prof);
        if (!profRepository.existsById(prof.getCodeprof())) {
            throw new IllegalArgumentException("Professeur introuvable (code " + prof.getCodeprof() + ").");
        }
        verifierUnicite(prof.getNom(), prof.getPrenom(), prof.getCodeprof());
        profRepository.update(prof);
    }

    public void supprimer(Integer codeprof) {
        try {
            profRepository.deleteById(codeprof);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "Impossible de supprimer ce professeur : des occupations lui sont associées. "
                            + "Supprimez d'abord ses occupations.");
        }
    }

    private void valider(Prof prof) {
        if (prof == null) {
            throw new IllegalArgumentException("Les données du professeur sont manquantes.");
        }
        String nom = prof.getNom() == null ? "" : prof.getNom().trim();
        String prenom = prof.getPrenom() == null ? "" : prof.getPrenom().trim();
        if (nom.isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire.");
        }
        if (!NOM_PATTERN.matcher(nom).matches()) {
            throw new IllegalArgumentException(
                    "Le nom doit contenir entre 2 et 100 lettres (espaces, tirets et apostrophes autorisés).");
        }
        if (!prenom.isEmpty() && !NOM_PATTERN.matcher(prenom).matches()) {
            throw new IllegalArgumentException(
                    "Le prénom doit contenir entre 2 et 100 lettres (espaces, tirets et apostrophes autorisés).");
        }
        prof.setNom(nom);
        prof.setPrenom(prenom);
        if (prof.getGrade() != null && !GRADES.contains(prof.getGrade())) {
            throw new IllegalArgumentException("Grade invalide. Valeurs acceptées : " + String.join(", ", GRADES) + ".");
        }
    }

    private void verifierUnicite(String nom, String prenom, Integer codeExclu) {
        boolean existe;
        if (prenom == null || prenom.isBlank()) {
            existe = profRepository.findByNomExact(nom).stream()
                    .filter(p -> p.getPrenom() == null || p.getPrenom().isBlank())
                    .anyMatch(p -> !p.getCodeprof().equals(codeExclu));
        } else {
            existe = profRepository.findByNomPrenom(nom, prenom).stream()
                    .anyMatch(p -> !p.getCodeprof().equals(codeExclu));
        }
        if (existe) {
            String libelle = prenom == null || prenom.isBlank() ? nom : nom + " " + prenom;
            throw new IllegalStateException("Un professeur nommé « " + libelle + " » existe déjà.");
        }
    }
}
