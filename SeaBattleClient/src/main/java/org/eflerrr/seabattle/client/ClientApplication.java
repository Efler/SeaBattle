package org.eflerrr.seabattle.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.tuple.Pair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientApplication extends Application {
    public enum Opponent {
        PLAYER(10),
        BOT(11);
        private final int id;

        Opponent(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public static Opponent chosenOpp = Opponent.PLAYER;
    private static Stage mainStage = null;
    private static Socket socket = null;
    private static DataInputStream serverReader = null;
    private static DataOutputStream serverWriter = null;
    private static ExecutorService service = null;      // TODO: check usage, overwise delete!


    private static void closeConnection() {
        try {
            if (serverReader != null) {
                serverReader.close();
            }
            if (serverWriter != null) {
                serverWriter.close();
            }
            if (socket != null && socket.isConnected()) {
                socket.close();
            }
            if (service != null && !service.isShutdown()) {
                service.shutdown();
                if (!service.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                    service.shutdownNow();
                }
            }
        } catch (InterruptedException iEx) {
            service.shutdownNow();
        } catch (IOException ioEx) {
            System.out.printf("Invalid socket closing! Message: %s%n", ioEx.getMessage());
        }
    }

    private static void loadingProcess(String message, String sceneFile, int sec, Label label)
            throws InterruptedException, IOException {
        AtomicInteger seconds = new AtomicInteger(sec);
        while (seconds.get() > 0) {
            Platform.runLater(() -> label.setText(String.format("%s %d...", message, seconds.getAndDecrement())));
            Thread.sleep(1000);
        }
        var sceneWidth = mainStage.getScene().getWidth();
        var sceneHeight = mainStage.getScene().getHeight();
        Scene mainScene = new Scene(FXMLLoader.load(
                Objects.requireNonNull(ClientApplication.class.getResource(sceneFile))),
                sceneWidth, sceneHeight);
        Platform.runLater(() -> mainStage.setScene(mainScene));
    }

    public static void lastPreparing() {
        new Thread(new Task<Void>() {
            @Override
            protected Void call() throws IOException, InterruptedException {
                var response = serverReader.readInt();
                var errorHBox = (HBox) (((VBox) (((HBox) (((VBox) (
                        mainStage.getScene().getRoot())).getChildren().get(1))).getChildren().get(1))).getChildren().get(4));
                if (response == 1) {
                    if (!errorHBox.isVisible()) {
                        Platform.runLater(() -> errorHBox.setVisible(true));
                    }
                    // TODO: change color!
                    Platform.runLater(() -> ((Label) (errorHBox.getChildren().getLast())).setText("Ждём соперника..."));
                    response = serverReader.readInt();
                    if (response == 0) {
                        loadingProcess("Соперник готов, начинаем сражение через", "battleScene.fxml",
                                3, ((Label) (errorHBox.getChildren().getLast())));
                    }
                } else {
                    if (!errorHBox.isVisible()) {
                        Platform.runLater(() -> errorHBox.setVisible(true));
                    }
                    loadingProcess("Отлично, начинаем сражение через", "battleScene.fxml",
                            3, ((Label) (errorHBox.getChildren().getLast())));
                }
                return null;
            }
        }).start();
    }

    public static int placeShip(Set<Pair<Integer, Integer>> coords, int cellsInShip) throws IOException {
        if (coords.size() != cellsInShip) {
            return 2;
        }
        serverWriter.writeInt(cellsInShip);
        for (var c : coords) {
            serverWriter.writeInt(c.getLeft());
            serverWriter.writeInt(c.getRight());
        }
        return serverReader.readInt();
    }

    public static void connectingProcess() {
        var label = ((Label) (((VBox) (((VBox) (
                mainStage.getScene().getRoot())).getChildren().getLast())).getChildren().getFirst())
        );
        try {
            socket = new Socket("localhost", 12345);
            serverReader = new DataInputStream(socket.getInputStream());
            serverWriter = new DataOutputStream(socket.getOutputStream());

            new Thread(new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        serverWriter.writeInt(chosenOpp.getId());   // TODO: [new]
                        int connectionNumber = serverReader.readInt();
                        if (connectionNumber == 1) {
                            Platform.runLater(() -> label.setText("Ждём соперника..."));
                            int serverNotification = serverReader.readInt();
                            if (serverNotification != 0) {
                                loadingProcess("Что-то пошло не так, возврат через", "mainScene.fxml",
                                        3, label);
                                closeConnection();
                                return null;
                            }
                        } else if (connectionNumber != 2) {
                            loadingProcess("Ошибка сервера, возврат через", "mainScene.fxml",
                                    3, label);
                            closeConnection();
                            return null;
                        }
                        loadingProcess("Соперник найден, начинаем через", "preparingScene.fxml",
                                3, label);
                    } catch (IOException | InterruptedException e) {
                        closeConnection();
                        System.out.printf("Invalid returning! Message: %s%n", e.getMessage());
                    }
                    return null;
                }
            }).start();
        } catch (IOException ex) {
            new Thread(new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        loadingProcess("Сервер недоступен, возврат через", "mainScene.fxml",
                                3, label);
                        closeConnection();
                        System.out.printf("Invalid connection to server! Message: %s%n", ex.getMessage());
                    } catch (IOException | InterruptedException e) {
                        closeConnection();
                        System.out.printf("Invalid returning! Message: %s%n", e.getMessage());
                    }
                    return null;
                }
            }).start();
        }
    }

    @Override
    public void start(Stage mainStage) throws IOException {
        service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ClientApplication.mainStage = mainStage;
        Scene mainScene = new Scene(
                FXMLLoader.load(Objects.requireNonNull(getClass().getResource("mainScene.fxml")))
        );
        ClientApplication.mainStage.setTitle("Sea Battle");
        ClientApplication.mainStage.setScene(mainScene);
        ClientApplication.mainStage.show();
    }

    @Override
    public void stop() {
        closeConnection();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch();
    }
}
