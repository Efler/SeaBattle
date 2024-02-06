package org.eflerrr.seabattle.client.controller;

import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ConnectingSceneController {
    @FXML
    public VBox connectingSceneVBox;
    @FXML
    public Label connectingSceneTitle;
    @FXML
    public VBox connectingSceneLoadVBox;
    @FXML
    public Label connectingSceneLoadLabel;
    @FXML
    public MFXProgressSpinner connectingSceneProgressSpinner;
}
