package com.bank;

import com.bank.models.Admins;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    
    // Maintain a static reference to the main window stage so controllers can switch views
    private static Stage primaryStage;

    private static Admins currentLoggedInUser = null;

    public static Admins getCurrentLoggedInUser() {
        return currentLoggedInUser;
    }

    public static void setCurrentLoggedInUser(Admins admin) {
        currentLoggedInUser = admin;
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bank/views/sign.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Bank System - Authorization");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    //switch screens cleanly from any controller
    public static void changeRootScene(String fxmlPath, String windowTitle, boolean resizable) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlPath));
            Parent newRoot = loader.load();
            
            primaryStage.getScene().setRoot(newRoot);
            primaryStage.setTitle(windowTitle);
            primaryStage.setResizable(resizable);
            
            // Adjust window size if switching to a much larger screen layout structure
            primaryStage.sizeToScene(); 
            primaryStage.centerOnScreen();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Critical Error: Navigation view route failed for " + fxmlPath);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}

