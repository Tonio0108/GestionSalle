package com.example.gestion.controller;

import com.example.gestion.model.Prof;
import com.example.gestion.service.ProfService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

public class ProfDialog extends Dialog<Prof> {

    private final ProfService profService;
    private final Prof existant;
    private final TextField txtNom = new TextField();
    private final TextField txtPrenom = new TextField();
    private final ComboBox<String> cmbGrade = new ComboBox<>();
    private final Label lblErreur = new Label();
    private Prof resultat;

    public ProfDialog(ProfService profService, Prof existant) {
        this.profService = profService;
        this.existant = existant;

        boolean modification = existant != null;
        setTitle(modification ? "Modifier un professeur" : "Nouveau professeur");
        setHeaderText(modification
                ? "Modification du professeur (code " + existant.getCodeprof() + ")"
                : "Renseignez les informations du nouveau professeur");

        DialogPane pane = getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
        pane.getStyleClass().add("dialog-surface");

        txtNom.setPromptText("Nom du professeur");
        txtPrenom.setPromptText("Prénom du professeur");
        txtNom.setMaxWidth(Double.MAX_VALUE);
        txtPrenom.setMaxWidth(Double.MAX_VALUE);
        cmbGrade.getItems().setAll(ProfService.GRADES);
        cmbGrade.setPromptText("Sélectionner un grade");
        cmbGrade.setMaxWidth(Double.MAX_VALUE);
        lblErreur.setWrapText(true);

        if (modification) {
            txtNom.setText(existant.getNom());
            txtPrenom.setText(existant.getPrenom());
            cmbGrade.setValue(existant.getGrade());
        }

        GridPane grille = new GridPane();
        grille.setHgap(10);
        grille.setVgap(12);
        ColumnConstraints colonneLabels = new ColumnConstraints();
        colonneLabels.setMinWidth(85);
        ColumnConstraints colonneChamps = new ColumnConstraints();
        colonneChamps.setHgrow(Priority.ALWAYS);
        grille.getColumnConstraints().addAll(colonneLabels, colonneChamps);

        int ligne = 0;
        if (modification) {
            Label lblCode = new Label(String.valueOf(existant.getCodeprof()));
            lblCode.setStyle("-fx-font-weight: bold; -fx-text-fill: #55685a;");
            grille.add(new Label("Code :"), 0, ligne);
            grille.add(lblCode, 1, ligne++);
        }
        grille.add(new Label("Nom :"), 0, ligne);
        grille.add(txtNom, 1, ligne++);
        grille.add(new Label("Prénom :"), 0, ligne);
        grille.add(txtPrenom, 1, ligne++);
        grille.add(new Label("Grade :"), 0, ligne);
        grille.add(cmbGrade, 1, ligne);

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
        Platform.runLater(txtNom::requestFocus);
    }

    private boolean enregistrer() {
        try {
            Integer code = existant == null ? null : existant.getCodeprof();
            String nom = txtNom.getText() == null ? "" : txtNom.getText().trim();
            String prenom = txtPrenom.getText() == null ? "" : txtPrenom.getText().trim();
            Prof prof = new Prof(code, nom, prenom, cmbGrade.getValue());
            if (existant == null) {
                profService.ajouter(prof);
            } else {
                profService.modifier(prof);
            }
            resultat = prof;
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
