package com.example.gestion;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@SpringBootApplication
public class GestionApplication extends Application {

    private ConfigurableApplicationContext context;

    public static void main(String[] args) {
        Application.launch(GestionApplication.class, args);
    }

    @Override
    public void init() {
        context = new SpringApplicationBuilder(GestionApplication.class)
                .web(WebApplicationType.NONE)
                .headless(false)
                .run();
    }

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            Scene scene = new Scene(root, 1150, 720);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            stage.setTitle("Gestion des salles");
            stage.setMaximized(true);
            stage.setMinWidth(980);
            stage.setMinHeight(620);
            stage.setScene(scene);
            stage.show();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur au démarrage de l'interface", e);
        }
    }

    @Override
    public void stop() {
        context.close();
        Platform.exit();
    }
}
