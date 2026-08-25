package com.example.gestion.controller;

import com.example.gestion.model.Salle;
import com.example.gestion.service.SalleService;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SalleController {

    private static final ButtonType BOUTON_OUI = new ButtonType("Oui", ButtonBar.ButtonData.YES);
    private static final ButtonType BOUTON_NON = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);

    private final SalleService salleService;

    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnModifier;
    @FXML
    private Button btnSupprimer;
    @FXML
    private TableView<Salle> tableSalle;
    @FXML
    private TableColumn<Salle, Number> colCode;
    @FXML
    private TableColumn<Salle, String> colDesignation;
    @FXML
    private Label lblStatus;

    private final ObservableList<Salle> donnees = FXCollections.observableArrayList();
    private final PauseTransition effacementStatut = new PauseTransition(Duration.seconds(6));

    public SalleController(SalleService salleService) {
        this.salleService = salleService;
    }

    @FXML
    private void initialize() {
        colCode.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCodesal()));
        colDesignation.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDesignation()));

        tableSalle.setItems(donnees);
        tableSalle.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        btnModifier.disableProperty().bind(
                Bindings.size(tableSalle.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
        btnSupprimer.disableProperty().bind(
                tableSalle.getSelectionModel().selectedItemProperty().isNull());

        tableSalle.setOnMouseClicked(evenement -> {
            if (evenement.getClickCount() == 2) {
                Salle selection = tableSalle.getSelectionModel().getSelectedItem();
                if (selection != null) {
                    ouvrirModification(selection);
                }
            }
        });
        tableSalle.setOnKeyPressed(evenement -> {
            if (evenement.getCode() == KeyCode.DELETE) {
                supprimerSelection();
                evenement.consume();
            }
        });

        effacementStatut.setOnFinished(evenement -> lblStatus.setText(""));
        refresh();
    }

    public void refresh() {
        donnees.setAll(salleService.lister());
        statutInfo(donnees.size() + " salle(s) enregistrée(s)");
    }

    @FXML
    private void onAjouter() {
        SalleDialog dialog = new SalleDialog(salleService, null);
        dialog.initOwner(tableSalle.getScene().getWindow());
        dialog.showAndWait().ifPresent(creee -> {
            refresh();
            selectionnerParCode(creee.getCodesal());
            statutSucces("Salle ajoutée (code " + creee.getCodesal() + ")");
        });
    }

    @FXML
    private void onModifier() {
        Salle selection = tableSalle.getSelectionModel().getSelectedItem();
        if (selection != null) {
            ouvrirModification(selection);
        }
    }

    private void ouvrirModification(Salle salle) {
        SalleDialog dialog = new SalleDialog(salleService, salle);
        dialog.initOwner(tableSalle.getScene().getWindow());
        dialog.showAndWait().ifPresent(modifiee -> {
            refresh();
            selectionnerParCode(modifiee.getCodesal());
            statutSucces("Salle modifiée (code " + modifiee.getCodesal() + ")");
        });
    }

    @FXML
    private void onSupprimer() {
        supprimerSelection();
    }

    private void supprimerSelection() {
        List<Salle> cibles = List.copyOf(tableSalle.getSelectionModel().getSelectedItems());
        supprimerSalles(cibles);
    }

    private void supprimerSalles(List<Salle> cibles) {
        if (cibles.isEmpty()) {
            return;
        }
        String detail = cibles.stream()
                .map(Salle::getDesignation)
                .limit(8)
                .collect(Collectors.joining("\n"))
                + (cibles.size() > 8 ? "\n..." : "");
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                detail, BOUTON_OUI, BOUTON_NON);
        confirmation.setHeaderText("Supprimer " + cibles.size() + " salle(s) ?");
        confirmation.initOwner(tableSalle.getScene().getWindow());
        confirmation.showAndWait().filter(BOUTON_OUI::equals).ifPresent(reponse -> {
            int succes = 0;
            StringBuilder echecs = new StringBuilder();
            for (Salle cible : cibles) {
                try {
                    salleService.supprimer(cible.getCodesal());
                    succes++;
                } catch (IllegalArgumentException | IllegalStateException e) {
                    echecs.append("- ").append(cible.getDesignation()).append(" : ").append(e.getMessage()).append("\n");
                } catch (Exception e) {
                    echecs.append("- ").append(cible.getDesignation()).append(" : erreur inattendue.\n");
                }
            }
            refresh();
            if (echecs.isEmpty()) {
                statutSucces(succes + " salle(s) supprimée(s)");
            } else {
                statutErreur(succes + " supprimée(s), " + cibles.size() + " demandée(s)\n" + echecs);
            }
        });
    }

    private void selectionnerParCode(Integer codesal) {
        donnees.stream()
                .filter(s -> s.getCodesal().equals(codesal))
                .findFirst()
                .ifPresent(s -> {
                    tableSalle.getSelectionModel().clearSelection();
                    tableSalle.getSelectionModel().select(s);
                    tableSalle.scrollTo(s);
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
