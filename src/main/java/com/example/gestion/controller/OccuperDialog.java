package com.example.gestion.controller;

import com.example.gestion.model.Occuper;
import com.example.gestion.model.Prof;
import com.example.gestion.model.Salle;
import com.example.gestion.service.OccuperService;
import com.example.gestion.service.ProfService;
import com.example.gestion.service.SalleService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.util.StringConverter;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OccuperDialog extends Dialog<Occuper> {

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMAT_HEURE = DateTimeFormatter.ofPattern("HH:mm");
    private static final LocalDate DATE_MIN = LocalDate.of(2010, 1, 1);
    private static final LocalTime HEURE_MIN = LocalTime.of(7, 0);
    private static final LocalTime HEURE_MAX = LocalTime.of(19, 30);

    private final OccuperService occuperService;
    private final ProfService profService;
    private final SalleService salleService;
    private final Occuper existant;
    private final boolean modification;
    private final ComboBox<Prof> cmbProf = new ComboBox<>();
    private final ComboBox<Salle> cmbSalle = new ComboBox<>();
    private final ComboBox<LocalTime> cmbHeure = new ComboBox<>();
    private final DatePicker dpDate = new DatePicker(LocalDate.now());
    private final Label lblErreur = new Label();
    private Occuper resultat;

    public OccuperDialog(OccuperService occuperService, ProfService profService,
                         SalleService salleService, Occuper existant) {
        this.occuperService = occuperService;
        this.profService = profService;
        this.salleService = salleService;
        this.existant = existant;
        this.modification = existant != null;

        setTitle(modification ? "Modifier une occupation" : "Nouvelle occupation");
        setHeaderText(modification
                ? "Modification de l'occupation du " + existant.getDate().format(FORMAT_DATE)
                : "Enregistrez une nouvelle occupation de salle");

        DialogPane pane = getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
        pane.getStyleClass().add("dialog-surface");

        cmbProf.setMaxWidth(Double.MAX_VALUE);
        cmbSalle.setMaxWidth(Double.MAX_VALUE);
        cmbHeure.setMaxWidth(Double.MAX_VALUE);
        dpDate.setMaxWidth(Double.MAX_VALUE);
        dpDate.setEditable(false);
        lblErreur.setWrapText(true);

        configurerComboProf();
        configurerComboSalle();
        configurerComboHeure();
        configurerDatePicker();

        if (modification) {
            cmbProf.getItems().stream()
                    .filter(p -> p.getCodeprof().equals(existant.getCodeprof()))
                    .findFirst().ifPresent(cmbProf::setValue);
            cmbSalle.getItems().stream()
                    .filter(s -> s.getCodesal().equals(existant.getCodesal()))
                    .findFirst().ifPresent(cmbSalle::setValue);
            dpDate.setValue(existant.getDate());
            cmbHeure.setValue(existant.getHeure());
        }

        GridPane grille = new GridPane();
        grille.setHgap(10);
        grille.setVgap(12);
        ColumnConstraints colonneLabels = new ColumnConstraints();
        colonneLabels.setMinWidth(105);
        ColumnConstraints colonneChamps = new ColumnConstraints();
        colonneChamps.setHgrow(Priority.ALWAYS);
        grille.getColumnConstraints().addAll(colonneLabels, colonneChamps);

        int ligne = 0;
        grille.add(new Label("Professeur :"), 0, ligne);
        grille.add(cmbProf, 1, ligne++);
        grille.add(new Label("Salle :"), 0, ligne);
        grille.add(cmbSalle, 1, ligne++);
        grille.add(new Label("Date :"), 0, ligne);
        grille.add(dpDate, 1, ligne++);
        grille.add(new Label("Heure :"), 0, ligne);
        grille.add(cmbHeure, 1, ligne);

        VBox contenu = new VBox(10, grille, lblErreur);
        contenu.setPadding(new Insets(0, 4, 0, 4));
        pane.setContent(contenu);

        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button btnOk = (Button) pane.lookupButton(ButtonType.OK);
        btnOk.setText("Enregistrer");
        btnOk.getStyleClass().add("btn-primary");
        Button btnCancel = (Button) pane.lookupButton(ButtonType.CANCEL);
        btnCancel.setText("Annuler");
        btnCancel.getStyleClass().add("btn-ghost");

        btnOk.addEventFilter(ActionEvent.ACTION, evenement -> {
            if (!enregistrer()) {
                evenement.consume();
            }
        });

        setResultConverter(type -> type == ButtonType.OK ? resultat : null);
        Platform.runLater(cmbProf::requestFocus);
    }

    private void configurerComboProf() {
        cmbProf.getItems().setAll(profService.lister());
        cmbProf.setConverter(new StringConverter<>() {
            @Override
            public String toString(Prof prof) {
                return prof == null ? "" : prof.getCodeprof() + " - " + prof.getNomComplet();
            }

            @Override
            public Prof fromString(String string) {
                return null;
            }
        });
    }

    private void configurerComboSalle() {
        cmbSalle.getItems().setAll(salleService.lister());
        cmbSalle.setConverter(new StringConverter<>() {
            @Override
            public String toString(Salle salle) {
                return salle == null ? "" : salle.getCodesal() + " - " + salle.getDesignation();
            }

            @Override
            public Salle fromString(String string) {
                return null;
            }
        });
    }

    private void configurerComboHeure() {
        List<LocalTime> creneaux = new ArrayList<>();
        for (LocalTime t = HEURE_MIN; !t.isAfter(HEURE_MAX); t = t.plusMinutes(30)) {
            creneaux.add(t);
        }
        cmbHeure.getItems().setAll(creneaux);
        if (!modification) {
            cmbHeure.setValue(creneauParDefaut(creneaux));
        }
        cmbHeure.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalTime time) {
                return time == null ? "" : time.format(FORMAT_HEURE);
            }

            @Override
            public LocalTime fromString(String string) {
                return null;
            }
        });
    }

    private LocalTime creneauParDefaut(List<LocalTime> creneaux) {
        LocalTime maintenant = LocalTime.now();
        return creneaux.stream()
                .filter(t -> !t.isBefore(maintenant))
                .findFirst()
                .orElse(HEURE_MIN);
    }

    private void configurerDatePicker() {
        dpDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean vide) {
                super.updateItem(item, vide);
                LocalDate dateMax = LocalDate.now().plusMonths(6);
                setDisable(vide || item.isBefore(DATE_MIN) || item.isAfter(dateMax));
                if (!vide) {
                    setTooltip(new Tooltip(item.format(FORMAT_DATE)));
                }
            }
        });
    }

    private boolean enregistrer() {
        try {
            Prof prof = cmbProf.getValue();
            Salle salle = cmbSalle.getValue();
            LocalDate date = dpDate.getValue();
            LocalTime heure = cmbHeure.getValue();
            if (prof == null || salle == null || date == null || heure == null) {
                throw new IllegalArgumentException("Le professeur, la salle, la date et l'heure sont obligatoires.");
            }
            Occuper occuper = new Occuper(prof.getCodeprof(), salle.getCodesal(), date, heure);
            if (existant == null) {
                occuperService.enregistrer(occuper);
            } else {
                occuperService.modifier(existant.getCodeprof(), existant.getCodesal(),
                        existant.getDate(), existant.getHeure(), occuper);
            }
            resultat = occuper;
            return true;
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblErreur.setText(e.getMessage());
            return false;
        } catch (Exception e) {
            lblErreur.setText("Une erreur inattendue est survenue.");
            return false;
        }
    }
}
