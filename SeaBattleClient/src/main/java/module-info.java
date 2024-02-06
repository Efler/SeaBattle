module org.eflerrr.seabattle.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires MaterialFX;
    requires org.apache.commons.lang3;

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.bootstrapicons;

    opens org.eflerrr.seabattle.client to javafx.fxml;
    exports org.eflerrr.seabattle.client;
    exports org.eflerrr.seabattle.client.controller;
    opens org.eflerrr.seabattle.client.controller to javafx.fxml;
}