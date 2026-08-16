package com.bank;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // This loads the FXML file you exported from Scene Builder
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bank/views/MainView.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Bank app");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
