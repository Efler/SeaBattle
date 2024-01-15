package org.eflerrr.seabattle.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ClientApplication extends Application {
    @Override
    public void start(Stage mainStage) throws IOException {
        Scene mainScene = new Scene(
                FXMLLoader.load(Objects.requireNonNull(getClass().getResource("mainScene.fxml")))
        );
        mainStage.setTitle("Sea Battle");
        mainStage.setScene(mainScene);
        mainStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}