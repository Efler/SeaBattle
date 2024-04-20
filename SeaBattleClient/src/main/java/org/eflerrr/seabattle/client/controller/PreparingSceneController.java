package org.eflerrr.seabattle.client.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXProgressBar;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.tuple.Pair;
import org.eflerrr.seabattle.client.ClientApplication;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

// TODO: make a changing preparingSceneErrorIcon to a progress spinner!

public class PreparingSceneController {
    @FXML
    public VBox preparingSceneVBox;
    @FXML
    public GridPane preparingSceneGridPane;
    @FXML
    public Label preparingSceneTitle;
    @FXML
    public Label preparingSceneLittleTitle;
    @FXML
    public Label preparingSceneTaskLabel;
    @FXML
    public Label preparingSceneCountLabel;
    @FXML
    public Label preparingSceneDescriptionLabel;
    @FXML
    public MFXButton preparingSceneConfirmButton;
    @FXML
    public MFXProgressBar preparingSceneProgressBar;
    @FXML
    public HBox preparingSceneErrorHBox;
    @FXML
    public FontIcon preparingSceneErrorIcon;
    @FXML
    public Label preparingSceneErrorLabel;

    private ConcurrentSkipListSet<Pair<Integer, Integer>> selectedCellsCoords = new ConcurrentSkipListSet<>((o1, o2) -> {
        if (o1.getLeft().equals(o2.getLeft())) {
            return o1.getRight() - o2.getRight();
        }
        return o1.getLeft() - o2.getLeft();
    });
    private Set<MFXButton> selectedCellsObjects = new HashSet<>();
    private int cellsInShip = 4;
    private int cellsAvailable = 4;
    private int shipsAvailable = 1;
    private int step = 1;
    private Map<Integer, String> ERRORS = new HashMap<>();


    public void onCellClicked(ActionEvent actionEvent) {
        var cell = (MFXButton) actionEvent.getSource();
        var cellCoords = Pair.of(
                GridPane.getRowIndex(cell),
                GridPane.getColumnIndex(cell)
        );
        var cellStyles = cell.getStyleClass();

        if (cellStyles.getLast().equals("emptyCell") && cellsAvailable > 0) {
            --cellsAvailable;
            selectedCellsObjects.add(cell);
            selectedCellsCoords.add(cellCoords);
            cellStyles.removeLast();
            cellStyles.add("chosenCell");
        } else if (cellStyles.getLast().equals("chosenCell")) {
            ++cellsAvailable;
            selectedCellsObjects.remove(cell);
            selectedCellsCoords.remove(cellCoords);
            cellStyles.removeLast();
            cellStyles.add("emptyCell");
        }
    }

    private Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> getUnavailableCells() {
        int x1 = Math.max((selectedCellsCoords.first().getLeft() - 1), 0);
        int x2 = Math.min((selectedCellsCoords.last().getLeft() + 1), 9);
        int y1 = Math.max((selectedCellsCoords.first().getRight() - 1), 0);
        int y2 = Math.min((selectedCellsCoords.last().getRight() + 1), 9);
        return Pair.of(Pair.of(x1, x2), Pair.of(y1, y2));
    }

    public void onConfirmClicked(ActionEvent actionEvent) {
        if (!preparingSceneErrorHBox.isVisible()) {                      // TODO: refactor!
            preparingSceneErrorHBox.setVisible(true);                    // TODO: refactor!
        }                                                                // TODO: refactor!
        preparingSceneErrorLabel.setText("Waiting server response...");  // TODO: refactor!
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws IOException {
                return ClientApplication.placeShip(selectedCellsCoords, cellsInShip);
            }
        };
        task.setOnSucceeded(e -> {
            int status = task.getValue();
            if (status == 0 || status == -1) {
                if (preparingSceneErrorHBox.isVisible()) {
                    Platform.runLater(() -> preparingSceneErrorHBox.setVisible(false));
                }

                --shipsAvailable;
                selectedCellsObjects.forEach((x) -> Platform.runLater(() -> x.setDisable(true)));

                var unavailableCellsBounds = getUnavailableCells();
                Set<Pair<Integer, Integer>> unavailableCellsCoords = new HashSet<>();
                for (int x = unavailableCellsBounds.getLeft().getLeft(); x < unavailableCellsBounds.getLeft().getRight() + 1; ++x) {
                    for (int y = unavailableCellsBounds.getRight().getLeft(); y < unavailableCellsBounds.getRight().getRight() + 1; ++y) {
                        unavailableCellsCoords.add(Pair.of(x, y));
                    }
                }
                unavailableCellsCoords.removeAll(selectedCellsCoords);
                for (Node node : preparingSceneGridPane.getChildren()) {
                    if (unavailableCellsCoords.contains(Pair.of(GridPane.getRowIndex(node), GridPane.getColumnIndex(node)))) {
                        Platform.runLater(() -> node.setDisable(true));
                    }
                }

                selectedCellsCoords.clear();
                selectedCellsObjects.clear();
                if (shipsAvailable > 0) {
                    cellsAvailable = cellsInShip;
                    Platform.runLater(() -> preparingSceneCountLabel.setText(String.format("%d", shipsAvailable)));
                } else {
                    if (step < 4) {
                        ++step;
                        shipsAvailable = step;
                        cellsInShip = 5 - step;
                        cellsAvailable = cellsInShip;
                        Platform.runLater(() -> {
                            preparingSceneProgressBar.setProgress(preparingSceneProgressBar.getProgress() + 0.25);
                            preparingSceneLittleTitle.setText(String.format("Этап %d", step));
                            preparingSceneTaskLabel.setText(String.format("Расставьте %dх-палубные корабли", cellsInShip));
                            preparingSceneCountLabel.setText(String.format("%d", shipsAvailable));
                        });
                    } else {
                        Platform.runLater(() ->
                                preparingSceneProgressBar.setProgress(preparingSceneProgressBar.getProgress() + 0.25)
                        );
                        ClientApplication.lastPreparing();
                    }
                }
            } else {
                if (!preparingSceneErrorHBox.isVisible()) {
                    preparingSceneErrorHBox.setVisible(true);
                }
                Platform.runLater(() -> preparingSceneErrorLabel.setText(ERRORS.get(status)));
            }
        });

        (new Thread(task)).start();
    }

    @FXML
    protected void initialize() {
        preparingSceneTaskLabel.setText(String.format("Расставьте %dх-палубные корабли", cellsInShip));
        preparingSceneCountLabel.setText(String.format("%d", shipsAvailable));
        preparingSceneLittleTitle.setText(String.format("Этап %d", step));
        preparingSceneErrorHBox.setVisible(false);

        ERRORS.put(1, "Неверная форма корабля!");
        ERRORS.put(2, "Неверный размер корабля!");
    }
}
