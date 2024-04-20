package org.eflerrr.seabattle.client.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.eflerrr.seabattle.client.ClientApplication;

import java.io.IOException;
import java.util.Objects;

public class MainSceneController {
    @FXML
    public VBox mainSceneVBox;
    @FXML
    public Label mainSceneTitle;
    @FXML
    public VBox mainSceneButtonsVBox;
    @FXML
    public MFXButton mainSceneStartButton;
    @FXML
    public MFXButton mainSceneRulesButton;
    @FXML
    public MFXToggleButton mainSceneOppToggleButton;

    public void onStartGameClick(ActionEvent actionEvent) throws IOException {
        ClientApplication.chosenOpp = mainSceneOppToggleButton.isSelected()
                ? ClientApplication.Opponent.PLAYER
                : ClientApplication.Opponent.BOT;
        Stage mainStage = (Stage) ((Node) (actionEvent.getSource())).getScene().getWindow();
        var sceneWidth = mainStage.getScene().getWidth();
        var sceneHeight = mainStage.getScene().getHeight();
        Scene connectingScene = new Scene(FXMLLoader.load(
                Objects.requireNonNull(ClientApplication.class.getResource("connectingScene.fxml"))),
                sceneWidth, sceneHeight);
        mainStage.setScene(connectingScene);
        ClientApplication.connectingProcess();
    }

    @FXML
    protected void initialize() {
        mainSceneOppToggleButton.setSelected(true);
    }

    public void onRulesClick(ActionEvent actionEvent) {

        // TODO: impl!

        Scene currentScene = ((Node) (actionEvent.getSource())).getScene();
        System.out.printf("%f %f%n", currentScene.getWidth(), currentScene.getHeight());
    }
}