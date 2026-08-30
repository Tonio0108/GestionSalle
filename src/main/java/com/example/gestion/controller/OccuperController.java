package com.example.gestion.controller;

import com.example.gestion.model.Occuper;
import com.example.gestion.service.OccuperService;
import com.example.gestion.service.ProfService;
import com.example.gestion.service.SalleService;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OccuperController {

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final ButtonType BOUTON_OUI = new ButtonType("Oui", ButtonBar.ButtonData.YES);
    private static final ButtonType BOUTON_NON = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);

    private final OccuperService occuperService;
    private final ProfService profService;
    private final SalleService salleService;

    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnModifier;
    @FXML
    private Button btnSupprimer;
    @FXML
    private TableView<Occuper> tableOccuper;
    @FXML
    private TableColumn<Occuper, Number> colCodeProf;
    @FXML
    private TableColumn<Occuper, String> colProf;
    @FXML
    private TableColumn<Occuper, Number> colCodeSalle;
    @FXML
    private TableColumn<Occuper, String> colSalle;
    @FXML
    private TableColumn<Occuper, LocalDate> colDate;
    @FXML
    private Label lblStatus;

    private final ObservableList<Occuper> donnees = FXCollections.observableArrayList();
    private final PauseTransition effacementStatut = new PauseTransition(Duration.seconds(6));

    public OccuperController(OccuperService occuperService, ProfService profService, SalleService salleService) {
        this.occuperService = occuperService;
        this.profService = profService;
        this.salleService = salleService;
    }

    @FXML
    private void initialize() {
        colCodeProf.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCodeprof()));
        colProf.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomProf()));
        colCodeSalle.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCodesal()));
        colSalle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDesignationSalle()));
        colDate.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getDate()));
        colDate.setCellFactory(colonne -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean vide) {
                super.updateItem(item, vide);
                setText(vide || item == null ? null : item.format(FORMAT_DATE));
            }
        });

        tableOccuper.setItems(donnees);
        tableOccuper.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        btnModifier.disableProperty().bind(
                Bindings.size(tableOccuper.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
        btnSupprimer.disableProperty().bind(
                tableOccuper.getSelectionModel().selectedItemProperty().isNull());

        tableOccuper.setOnMouseClicked(evenement -> {
            if (evenement.getClickCount() == 2) {
                Occuper selection = tableOccuper.getSelectionModel().getSelectedItem();
                if (selection != null) {
                    ouvrirModification(selection);
                }
            }
        });
        tableOccuper.setOnKeyPressed(evenement -> {
            if (evenement.getCode() == KeyCode.DELETE) {
                supprimerSelection();
                evenement.consume();
            }
        });

        effacementStatut.setOnFinished(evenement -> lblStatus.setText(""));
        refresh();
    }

    public void refresh() {
        donnees.setAll(occuperService.lister());
        statutInfo(donnees.size() + " occupation(s) enregistrée(s)");
    }

    @FXML
    private void onAjouter() {
        OccuperDialog dialog = new OccuperDialog(occuperService, profService, salleService, null);
        dialog.initOwner(tableOccuper.getScene().getWindow());
        dialog.showAndWait().ifPresent(creee -> {
            refresh();
            selectionnerOccupation(creee.getCodeprof(), creee.getCodesal(), creee.getDate());
            statutSucces("Occupation enregistrée");
        });
    }

    @FXML
    private void onModifier() {
        Occuper selection = tableOccuper.getSelectionModel().getSelectedItem();
        if (selection != null) {
            ouvrirModification(selection);
        }
    }

    private void ouvrirModification(Occuper occuper) {
        OccuperDialog dialog = new OccuperDialog(occuperService, profService, salleService, occuper);
        dialog.initOwner(tableOccuper.getScene().getWindow());
        dialog.showAndWait().ifPresent(modifiee -> {
            refresh();
            selectionnerOccupation(modifiee.getCodeprof(), modifiee.getCodesal(), modifiee.getDate());
            statutSucces("Occupation modifiée");
        });
    }

    @FXML
    private void onSupprimer() {
        supprimerSelection();
    }

    private void supprimerSelection() {
        List<Occuper> cibles = List.copyOf(tableOccuper.getSelectionModel().getSelectedItems());
        supprimerOccupations(cibles);
    }

    private void supprimerOccupations(List<Occuper> cibles) {
        if (cibles.isEmpty()) {
            return;
        }
        String detail = cibles.stream()
                .map(o -> o.getNomProf() + " — " + o.getDesignationSalle()
                        + " — " + o.getDate().format(FORMAT_DATE))
                .limit(8)
                .collect(Collectors.joining("\n"))
                + (cibles.size() > 8 ? "\n..." : "");
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                detail, BOUTON_OUI, BOUTON_NON);
        confirmation.setHeaderText("Supprimer " + cibles.size() + " occupation(s) ?");
        confirmation.initOwner(tableOccuper.getScene().getWindow());
        confirmation.showAndWait().filter(BOUTON_OUI::equals).ifPresent(reponse -> {
            int succes = 0;
            StringBuilder echecs = new StringBuilder();
            for (Occuper cible : cibles) {
                try {
                    occuperService.supprimer(cible.getCodeprof(), cible.getCodesal(),
                            cible.getDate());
                    succes++;
                } catch (Exception e) {
                    echecs.append("- ").append(cible.getNomProf()).append(" (")
                            .append(cible.getDate().format(FORMAT_DATE))
                            .append(") : erreur.\n");
                }
            }
            refresh();
            if (echecs.isEmpty()) {
                statutSucces(succes + " occupation(s) supprimée(s)");
            } else {
                statutErreur(succes + " supprimée(s), " + cibles.size() + " demandée(s)\n" + echecs);
            }
        });
    }

    private void selectionnerOccupation(Integer codeprof, Integer codesal, LocalDate date) {
        donnees.stream()
                .filter(o -> o.getCodeprof().equals(codeprof)
                        && o.getCodesal().equals(codesal)
                        && o.getDate().equals(date))
                .findFirst()
                .ifPresent(o -> {
                    tableOccuper.getSelectionModel().clearSelection();
                    tableOccuper.getSelectionModel().select(o);
                    tableOccuper.scrollTo(o);
                });
    }

    private void statutSucces(String message) {
        appliquerStatut("status-success", message);
    }

    private void statutErreur(String message) {
        appliquerStatut("status-error", message);
    }

    private void statutInfo(String message) {
        appliquerStatut("status-info", message);
    }

    private void appliquerStatut(String classeCss, String message) {
        lblStatus.getStyleClass().removeAll("status-success", "status-error", "status-info");
        lblStatus.getStyleClass().add(classeCss);
        lblStatus.setText(message);
        effacementStatut.playFromStart();
    }
}
