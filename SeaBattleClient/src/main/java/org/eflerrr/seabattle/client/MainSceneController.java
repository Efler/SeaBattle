package org.eflerrr.seabattle.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainSceneController {
    public enum Opponent {
        PLAYER,
        BOT
    }
    public Opponent chosenOpp = Opponent.PLAYER;

    @FXML
    public VBox mainSceneVBox;
    @FXML
    public Label mainSceneTitle;
    @FXML
    public VBox mainSceneButtonsVBox;
    @FXML
    public Button mainSceneStartButton;
    @FXML
    public Label mainSceneOppLabel;
    @FXML
    public HBox mainSceneOppHBox;
    @FXML
    public ToggleButton mainSceneOppPlayerButton;
    @FXML
    public ToggleButton mainSceneOppBotButton;
    @FXML
    public Button mainSceneRulesButton;


    public void onStartGameClick(ActionEvent actionEvent) throws IOException {
        Stage mainStage = (Stage)((Node)(actionEvent.getSource())).getScene().getWindow();
        Scene preparingScene = new Scene(FXMLLoader.load(
                Objects.requireNonNull(getClass().getResource("preparingScene.fxml"))));
        mainStage.setScene(preparingScene);
    }

    public void onOppPlayerChoose() {
        if (chosenOpp.equals(Opponent.PLAYER)) {
            mainSceneOppPlayerButton.setSelected(true);
        }
        else {
            chosenOpp = Opponent.PLAYER;
            mainSceneOppBotButton.setSelected(false);
        }
    }

    public void onOppBotChoose() {
        if (chosenOpp.equals(Opponent.BOT)) {
            mainSceneOppBotButton.setSelected(true);
        }
        else {
            chosenOpp = Opponent.BOT;
            mainSceneOppPlayerButton.setSelected(false);
        }
    }

    public void onRulesClick(ActionEvent actionEvent) {
        // TODO!
    }
}