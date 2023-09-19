package me.hostadam.mcstatus;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MCStatusApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MCStatusApplication.class.getResource("hello-view.fxml"));
        stage.setTitle("MCScanner 0.1 by Hostadam");
        stage.setScene(new Scene(fxmlLoader.load(), 400, 600));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}