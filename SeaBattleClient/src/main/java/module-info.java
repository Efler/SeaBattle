module org.eflerrr.seabattle.client {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.eflerrr.seabattle.client to javafx.fxml;
    exports org.eflerrr.seabattle.client;
}