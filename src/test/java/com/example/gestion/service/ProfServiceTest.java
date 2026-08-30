package com.example.gestion.service;

import com.example.gestion.model.Prof;
import com.example.gestion.repository.ProfRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfServiceTest {

    @Mock
    private ProfRepository profRepository;

    @InjectMocks
    private ProfService profService;

    @Test
    void lister_retourneTousLesProfs() {
        when(profRepository.findAll()).thenReturn(List.of(
                new Prof(1, "Dupont", "Jean", "Professeur"),
                new Prof(2, "Martin", "Marie", "Assistant")));

        assertThat(profService.lister()).hasSize(2);
    }

    @Test
    void trouverParCode_avecCodeValide_retourneLeProf() {
        Prof attendu = new Prof(1, "Dupont", "Jean", "Professeur");
        when(profRepository.findById(1)).thenReturn(Optional.of(attendu));

        assertThat(profService.trouverParCode(1)).isSameAs(attendu);
    }

    @Test
    void trouverParCode_avecCodeInvalide_leveException() {
        assertThatThrownBy(() -> profService.trouverParCode(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> profService.trouverParCode(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chercherParNom_avecNomVide_leveException() {
        assertThatThrownBy(() -> profService.chercherParNom("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chercherParNom_avecNomValide_retourneLesProfs() {
        when(profRepository.findByNom("dupont")).thenReturn(
                List.of(new Prof(1, "Dupont", "Jean", "Professeur")));

        assertThat(profService.chercherParNom("dupont")).hasSize(1);
    }

    @Test
    void ajouter_profValide_retourneProfAvecCode() {
        Prof prof = new Prof(null, "  Dupont  ", " Jean ", "Professeur");
        when(profRepository.findByNomPrenom("Dupont", "Jean")).thenReturn(List.of());
        when(profRepository.insert(prof)).thenReturn(5);

        Prof resultat = profService.ajouter(prof);

        assertThat(resultat.getCodeprof()).isEqualTo(5);
        assertThat(resultat.getNom()).isEqualTo("Dupont");
        assertThat(resultat.getPrenom()).isEqualTo("Jean");
    }

    @Test
    void ajouter_nomVide_leveException() {
        assertThatThrownBy(() -> profService.ajouter(new Prof(null, "", "Jean", "Professeur")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatoire");
    }

    @Test
    void ajouter_nomTropCourt_leveException() {
        assertThatThrownBy(() -> profService.ajouter(new Prof(null, "D", "Jean", "Professeur")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ajouter_gradeInvalide_leveException() {
        assertThatThrownBy(() -> profService.ajouter(new Prof(null, "Dupont", "Jean", "PDG")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Grade invalide");
    }

    @Test
    void ajouter_doublon_leveException() {
        Prof prof = new Prof(null, "Dupont", "Jean", "Professeur");
        when(profRepository.findByNomPrenom("Dupont", "Jean"))
                .thenReturn(List.of(new Prof(2, "Dupont", "Jean", "Assistant")));

        assertThatThrownBy(() -> profService.ajouter(prof))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("existe déjà");
    }

    @Test
    void modifier_profInexistant_leveException() {
        Prof prof = new Prof(99, "Dupont", "Jean", "Professeur");
        when(profRepository.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> profService.modifier(prof))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("introuvable");
    }

    @Test
    void modifier_profValide_appelleLeRepository() {
        Prof prof = new Prof(1, "Dupont", "Jean", "Professeur");
        when(profRepository.existsById(1)).thenReturn(true);
        when(profRepository.findByNomPrenom("Dupont", "Jean")).thenReturn(List.of());

        profService.modifier(prof);

        verify(profRepository).update(prof);
    }

    @Test
    void supprimer_avecOccupations_associees_traduitEnIllegalState() {
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("contrainte FK"))
                .when(profRepository).deleteById(1);

        assertThatThrownBy(() -> profService.supprimer(1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("occupations");
    }

    @Test
    void getNomComplet_avecNomEtPrenom() {
        assertThat(new Prof(1, "Dupont", "Jean", "Professeur").getNomComplet())
                .isEqualTo("Dupont Jean");
    }

    @Test
    void getNomComplet_sansDonnees_vide() {
        assertThat(new Prof(1, null, null, null).getNomComplet()).isEmpty();
    }
}
