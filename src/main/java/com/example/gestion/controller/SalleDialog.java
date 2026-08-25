package com.example.gestion.controller;

import com.example.gestion.model.Salle;
import com.example.gestion.service.SalleService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

public class SalleDialog extends Dialog<Salle> {

    private final SalleService salleService;
    private final Salle existant;
    private final TextField txtDesignation = new TextField();
    private final Label lblErreur = new Label();
    private Salle resultat;

    public SalleDialog(SalleService salleService, Salle existant) {
        this.salleService = salleService;
        this.existant = existant;

        boolean modification = existant != null;
        setTitle(modification ? "Modifier une salle" : "Nouvelle salle");
        setHeaderText(modification
                ? "Modification de la salle (code " + existant.getCodesal() + ")"
                : "Renseignez la désignation de la nouvelle salle");

        DialogPane pane = getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
        pane.getStyleClass().add("dialog-surface");

        txtDesignation.setPromptText("ex : Salle 101, Amphi A...");
        txtDesignation.setMaxWidth(Double.MAX_VALUE);
        lblErreur.setWrapText(true);

        if (modification) {
            txtDesignation.setText(existant.getDesignation());
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
        if (modification) {
            Label lblCode = new Label(String.valueOf(existant.getCodesal()));
            lblCode.setStyle("-fx-font-weight: bold; -fx-text-fill: #55685a;");
            grille.add(new Label("Code :"), 0, ligne);
            grille.add(lblCode, 1, ligne++);
        }
        grille.add(new Label("Désignation :"), 0, ligne);
        grille.add(txtDesignation, 1, ligne);

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
        Platform.runLater(txtDesignation::requestFocus);
    }

    private boolean enregistrer() {
        try {
            Integer code = existant == null ? null : existant.getCodesal();
            Salle salle = new Salle(code,
                    txtDesignation.getText() == null ? "" : txtDesignation.getText().trim());
            if (existant == null) {
                salleService.ajouter(salle);
            } else {
                salleService.modifier(salle);
            }
            resultat = salle;
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
