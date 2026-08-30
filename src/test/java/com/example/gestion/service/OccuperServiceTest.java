package com.example.gestion.service;

import com.example.gestion.model.Occuper;
import com.example.gestion.repository.OccuperRepository;
import com.example.gestion.repository.ProfRepository;
import com.example.gestion.repository.SalleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OccuperServiceTest {

    @Mock
    private OccuperRepository occuperRepository;

    @Mock
    private ProfRepository profRepository;

    @Mock
    private SalleRepository salleRepository;

    @InjectMocks
    private OccuperService occuperService;

    private Occuper valide() {
        return new Occuper(1, 1, LocalDate.now());
    }

    @Test
    void enregistrer_occupationValide_appelleLeRepository() {
        Occuper occuper = valide();
        when(profRepository.existsById(1)).thenReturn(true);
        when(salleRepository.existsById(1)).thenReturn(true);

        occuperService.enregistrer(occuper);

        verify(occuperRepository).insert(occuper);
    }

    @Test
    void enregistrer_champsManquants_leveException() {
        assertThatThrownBy(() -> occuperService.enregistrer(new Occuper(null, 1, LocalDate.now())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatoires");
        assertThatThrownBy(() -> occuperService.enregistrer(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void enregistrer_dateTropAncienne_leveException() {
        Occuper occuper = new Occuper(1, 1, LocalDate.of(2000, 1, 1));

        assertThatThrownBy(() -> occuperService.enregistrer(occuper))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("date");
    }

    @Test
    void enregistrer_dateTropLointaine_leveException() {
        Occuper occuper = new Occuper(1, 1, LocalDate.now().plusYears(2));

        assertThatThrownBy(() -> occuperService.enregistrer(occuper))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("date");
    }

    @Test
    void enregistrer_profInconnu_leveException() {
        Occuper occuper = valide();
        when(profRepository.existsById(1)).thenReturn(false);

        assertThatThrownBy(() -> occuperService.enregistrer(occuper))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Professeur introuvable");
    }

    @Test
    void enregistrer_salleInconnue_leveException() {
        Occuper occuper = valide();
        when(profRepository.existsById(1)).thenReturn(true);
        when(salleRepository.existsById(1)).thenReturn(false);

        assertThatThrownBy(() -> occuperService.enregistrer(occuper))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Salle introuvable");
    }

    @Test
    void enregistrer_doublon_traduitEnIllegalState() {
        Occuper occuper = valide();
        when(profRepository.existsById(1)).thenReturn(true);
        when(salleRepository.existsById(1)).thenReturn(true);
        org.mockito.Mockito.doThrow(
                new DuplicateKeyException("ux_occuper_salle_date"))
                .when(occuperRepository).insert(occuper);

        assertThatThrownBy(() -> occuperService.enregistrer(occuper))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("déjà occupée");
    }

    @Test
    void supprimer_appelleLeRepository() {
        LocalDate date = LocalDate.now();
        occuperService.supprimer(1, 1, date);
        verify(occuperRepository).delete(1, 1, date);
    }
}
