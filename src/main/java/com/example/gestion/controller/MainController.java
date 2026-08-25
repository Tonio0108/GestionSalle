package com.example.gestion.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MainController {

    private final ConfigurableApplicationContext context;

    @FXML
    private StackPane centerPane;

    @FXML
    private Button btnNavProf;

    @FXML
    private Button btnNavSalle;

    @FXML
    private Button btnNavOccuper;

    private Parent profView;
    private Parent salleView;
    private Parent occuperView;

    private ProfController profController;
    private SalleController salleController;
    private OccuperController occuperController;

    public MainController(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @FXML
    private void initialize() {
        profView = chargerVue("/views/prof.fxml");
        salleView = chargerVue("/views/salle.fxml");
        occuperView = chargerVue("/views/occuper.fxml");

        profController = context.getBean(ProfController.class);
        salleController = context.getBean(SalleController.class);
        occuperController = context.getBean(OccuperController.class);

        afficherProf();
    }

    private Parent chargerVue(String chemin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(chemin));
            loader.setControllerFactory(context::getBean);
            return loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de charger la vue " + chemin, e);
        }
    }

    @FXML
    private void onNavProf() {
        afficherProf();
    }

    @FXML
    private void onNavSalle() {
        afficherSalle();
    }

    @FXML
    private void onNavOccuper() {
        afficherOccuper();
    }

    private void afficherProf() {
        centerPane.getChildren().setAll(profView);
        marquerActif(btnNavProf);
        profController.refresh();
    }

    private void afficherSalle() {
        centerPane.getChildren().setAll(salleView);
        marquerActif(btnNavSalle);
        salleController.refresh();
    }

    private void afficherOccuper() {
        centerPane.getChildren().setAll(occuperView);
        marquerActif(btnNavOccuper);
        occuperController.refresh();
    }

    private void marquerActif(Button actif) {
        for (Button bouton : new Button[]{btnNavProf, btnNavSalle, btnNavOccuper}) {
            bouton.getStyleClass().remove("active");
        }
        if (!actif.getStyleClass().contains("active")) {
            actif.getStyleClass().add("active");
        }
    }
}
