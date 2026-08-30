package com.example.gestion.service;

import com.example.gestion.model.Salle;
import com.example.gestion.repository.SalleRepository;
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
class SalleServiceTest {

    @Mock
    private SalleRepository salleRepository;

    @InjectMocks
    private SalleService salleService;

    @Test
    void lister_retourneToutesLesSalles() {
        when(salleRepository.findAll())
                .thenReturn(List.of(new Salle(1, "Salle A"), new Salle(2, "Salle B")));

        List<Salle> resultat = salleService.lister();

        assertThat(resultat).hasSize(2);
        assertThat(resultat).extracting(Salle::getDesignation)
                .containsExactly("Salle A", "Salle B");
    }

    @Test
    void trouverParCode_avecCodeValide_retourneLaSalle() {
        Salle attendue = new Salle(1, "Salle A");
        when(salleRepository.findById(1)).thenReturn(Optional.of(attendue));

        Salle resultat = salleService.trouverParCode(1);

        assertThat(resultat).isSameAs(attendue);
    }

    @Test
    void trouverParCode_avecCodeInconnu_retourneNull() {
        when(salleRepository.findById(99)).thenReturn(Optional.empty());

        assertThat(salleService.trouverParCode(99)).isNull();
    }

    @Test
    void trouverParCode_avecCodeInvalide_leveException() {
        assertThatThrownBy(() -> salleService.trouverParCode(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> salleService.trouverParCode(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> salleService.trouverParCode(-5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ajouter_salleValide_retourneSalleAvecCode() {
        Salle salle = new Salle(null, "  Salle A  ");
        when(salleRepository.findByDesignationExact("Salle A")).thenReturn(List.of());
        when(salleRepository.insert(salle)).thenReturn(7);

        Salle resultat = salleService.ajouter(salle);

        assertThat(resultat.getCodesal()).isEqualTo(7);
        assertThat(resultat.getDesignation()).isEqualTo("Salle A");
    }

    @Test
    void ajouter_designationVide_leveException() {
        assertThatThrownBy(() -> salleService.ajouter(new Salle(null, "  ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatoire");
    }

    @Test
    void ajouter_designationTropCourte_leveException() {
        assertThatThrownBy(() -> salleService.ajouter(new Salle(null, "A")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ajouter_doublon_leveException() {
        Salle salle = new Salle(null, "Salle A");
        when(salleRepository.findByDesignationExact("Salle A"))
                .thenReturn(List.of(new Salle(1, "Salle A")));

        assertThatThrownBy(() -> salleService.ajouter(salle))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("existe déjà");
    }

    @Test
    void modifier_salleInexistante_leveException() {
        Salle salle = new Salle(99, "Salle A");
        when(salleRepository.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> salleService.modifier(salle))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("introuvable");
    }

    @Test
    void modifier_salleValide_appelleLeRepository() {
        Salle salle = new Salle(1, "Salle A");
        when(salleRepository.existsById(1)).thenReturn(true);
        when(salleRepository.findByDesignationExact("Salle A")).thenReturn(List.of());

        salleService.modifier(salle);

        verify(salleRepository).update(salle);
    }

    @Test
    void supprimer_avecOccupation_associee_traduitEnIllegalState() {
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("contrainte FK"))
                .when(salleRepository).deleteById(1);

        assertThatThrownBy(() -> salleService.supprimer(1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("occupations");
    }

    @Test
    void toString_afficheCodeEtDesignation() {
        Salle salle = new Salle(3, "Salle C");

        assertThat(salle.toString())
                .contains("codesal=3")
                .contains("designation='Salle C'");
    }
}
