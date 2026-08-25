package com.example.gestion.controller;

import com.example.gestion.model.Prof;
import com.example.gestion.service.ProfService;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProfController {

    private static final ButtonType BOUTON_OUI = new ButtonType("Oui", ButtonBar.ButtonData.YES);
    private static final ButtonType BOUTON_NON = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);

    private final ProfService profService;

    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnModifier;
    @FXML
    private Button btnSupprimer;
    @FXML
    private ComboBox<String> cmbCritere;
    @FXML
    private TextField txtRecherche;
    @FXML
    private TableView<Prof> tableProf;
    @FXML
    private TableColumn<Prof, Number> colCode;
    @FXML
    private TableColumn<Prof, String> colNom;
    @FXML
    private TableColumn<Prof, String> colPrenom;
    @FXML
    private TableColumn<Prof, String> colGrade;
    @FXML
    private Label lblStatus;

    private final ObservableList<Prof> donnees = FXCollections.observableArrayList();
    private final PauseTransition debounceRecherche = new PauseTransition(Duration.millis(300));
    private final PauseTransition effacementStatut = new PauseTransition(Duration.seconds(6));

    public ProfController(ProfService profService) {
        this.profService = profService;
    }

    @FXML
    private void initialize() {
        colCode.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCodeprof()));
        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        colPrenom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPrenom()));
        colGrade.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getGrade() == null ? "—" : c.getValue().getGrade()));

        tableProf.setItems(donnees);
        tableProf.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);

        btnModifier.disableProperty().bind(
                Bindings.size(tableProf.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
        btnSupprimer.disableProperty().bind(
                tableProf.getSelectionModel().selectedItemProperty().isNull());

        tableProf.setOnMouseClicked(evenement -> {
            if (evenement.getClickCount() == 2) {
                Prof selection = tableProf.getSelectionModel().getSelectedItem();
                if (selection != null) {
                    ouvrirModification(selection);
                }
            }
        });
        tableProf.setOnKeyPressed(evenement -> {
            if (evenement.getCode() == KeyCode.DELETE) {
                supprimerSelection();
                evenement.consume();
            }
        });

        cmbCritere.getItems().addAll("Nom", "Code");
        cmbCritere.setValue("Nom");
        cmbCritere.valueProperty().addListener((obs, ancien, nouveau) ->
                txtRecherche.setPromptText("Code".equals(nouveau)
                        ? "Numéro de code exact..."
                        : "Tapez pour filtrer par nom..."));

        debounceRecherche.setOnFinished(evenement -> {
            if (!txtRecherche.getText().isBlank()) {
                executerRecherche();
            }
        });
        txtRecherche.textProperty().addListener((obs, ancien, nouveau) -> {
            if ("Nom".equals(cmbCritere.getValue())) {
                debounceRecherche.playFromStart();
            }
        });
        txtRecherche.setOnAction(evenement -> executerRecherche());

        effacementStatut.setOnFinished(evenement -> lblStatus.setText(""));
        refresh();
    }

    public void refresh() {
        donnees.setAll(profService.lister());
        statutInfo(donnees.size() + " professeur(s) enregistré(s)");
    }

    @FXML
    private void onAjouter() {
        ProfDialog dialog = new ProfDialog(profService, null);
        dialog.initOwner(tableProf.getScene().getWindow());
        dialog.showAndWait().ifPresent(cree -> {
            rafraichirApresAction();
            selectionnerParCode(cree.getCodeprof());
            statutSucces("Professeur ajouté (code " + cree.getCodeprof() + ")");
        });
    }

    @FXML
    private void onModifier() {
        Prof selection = tableProf.getSelectionModel().getSelectedItem();
        if (selection != null) {
            ouvrirModification(selection);
        }
    }

    private void ouvrirModification(Prof prof) {
        ProfDialog dialog = new ProfDialog(profService, prof);
        dialog.initOwner(tableProf.getScene().getWindow());
        dialog.showAndWait().ifPresent(modifie -> {
            rafraichirApresAction();
            selectionnerParCode(modifie.getCodeprof());
            statutSucces("Professeur modifié (code " + modifie.getCodeprof() + ")");
        });
    }

    @FXML
    private void onSupprimer() {
        supprimerSelection();
    }

    private void supprimerSelection() {
        List<Prof> cibles = List.copyOf(tableProf.getSelectionModel().getSelectedItems());
        supprimerProfs(cibles);
    }

    private void supprimerProfs(List<Prof> cibles) {
        if (cibles.isEmpty()) {
            return;
        }
        String detail = cibles.stream()
                .map(Prof::getNomComplet)
                .limit(8)
                .collect(Collectors.joining("\n"))
                + (cibles.size() > 8 ? "\n..." : "");
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                detail, BOUTON_OUI, BOUTON_NON);
        confirmation.setHeaderText("Supprimer " + cibles.size() + " professeur(s) ?");
        confirmation.initOwner(tableProf.getScene().getWindow());
        confirmation.showAndWait().filter(BOUTON_OUI::equals).ifPresent(reponse -> {
            int succes = 0;
            StringBuilder echecs = new StringBuilder();
            for (Prof cible : cibles) {
                try {
                    profService.supprimer(cible.getCodeprof());
                    succes++;
                } catch (IllegalArgumentException | IllegalStateException e) {
                    echecs.append("- ").append(cible.getNomComplet()).append(" : ").append(e.getMessage()).append("\n");
                } catch (Exception e) {
                    echecs.append("- ").append(cible.getNomComplet())
                            .append(" : erreur inattendue.\n");
                }
            }
            rafraichirApresAction();
            if (echecs.isEmpty()) {
                statutSucces(succes + " professeur(s) supprimé(s)");
            } else {
                statutErreur(succes + " supprimé(s), " + cibles.size() + " demandé(s)\n" + echecs);
            }
        });
    }

    @FXML
    private void onRechercher() {
        executerRecherche();
    }

    @FXML
    private void onAfficherTout() {
        txtRecherche.clear();
        refresh();
    }

    private void executerRecherche() {
        String critere = cmbCritere.getValue();
        String valeur = txtRecherche.getText() == null ? "" : txtRecherche.getText().trim();
        if (valeur.isEmpty()) {
            refresh();
            return;
        }
        List<Prof> resultats;
        if ("Code".equals(critere)) {
            int code;
            try {
                code = Integer.parseInt(valeur);
            } catch (NumberFormatException e) {
                statutErreur("Le code doit être un nombre entier.");
                return;
            }
            Prof prof = profService.trouverParCode(code);
            resultats = prof == null ? List.of() : List.of(prof);
        } else {
            resultats = profService.chercherParNom(valeur);
        }
        donnees.setAll(resultats);
        if (resultats.isEmpty()) {
            statutInfo("Aucun résultat pour « " + valeur + " »");
        } else {
            statutInfo(resultats.size() + " résultat(s) pour « " + valeur + " »");
        }
    }

    private void rafraichirApresAction() {
        String valeur = txtRecherche.getText();
        if (valeur != null && !valeur.isBlank() && "Nom".equals(cmbCritere.getValue())) {
            executerRecherche();
        } else {
            donnees.setAll(profService.lister());
        }
    }

    private void selectionnerParCode(Integer codeprof) {
        donnees.stream()
                .filter(p -> p.getCodeprof().equals(codeprof))
                .findFirst()
                .ifPresent(p -> {
                    tableProf.getSelectionModel().clearSelection();
                    tableProf.getSelectionModel().select(p);
                    tableProf.scrollTo(p);
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
