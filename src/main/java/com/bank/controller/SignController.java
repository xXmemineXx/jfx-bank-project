package com.bank.controller;

import com.bank.App;
import com.bank.dao.AdminDAO;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;

public class SignController {

    @FXML private Label formTitle;
    @FXML private VBox registerContainer;
    @FXML private Hyperlink toggleModeLink;

    @FXML private TextField firstnameField;
    @FXML private TextField lastnameField;
    @FXML private TextField mailField;
    @FXML private Label action;
    @FXML private PasswordField pswdField;
    @FXML private PasswordField confirmPswdField;

    private final AdminDAO adminDAO = new AdminDAO();
    private boolean isSignUpMode = false;

    @FXML
    public void initialize() {
        // Start in Sign-In mode by default: hide the registration fields
        setSignUpModeActive(false);
    }

    @FXML
    private void handleToggleMode() {
        isSignUpMode = !isSignUpMode;
        setSignUpModeActive(isSignUpMode);
    }

    private void setSignUpModeActive(boolean active) {
        // 1. Hide/Show the registration fields layout block
        registerContainer.setVisible(active);
        
        // CRUCIAL: Tell JavaFX to collapse the layout space when hidden
        registerContainer.setManaged(active);

        // 2. Dynamically transform the textual context strings
        if (active) {
            formTitle.setText("Create Administrator Account");
            action.setText("Register Account");
            toggleModeLink.setText("Already have an account? Sign in here");
            fadeInFields();
        } else {
            formTitle.setText("Sign In to Dashboard");
            action.setText("Login");
            toggleModeLink.setText("Don't have an account? Sign up here");
            registerContainer.setOpacity(0.0);
            registerContainer.setTranslateY(0.0);
        }
    }

    //log confirmation method
    @FXML
    private void confirmLog() {
        String email = mailField.getText();
        String password = pswdField.getText();

        if (isSignUpMode) {
            String firstName = firstnameField.getText();
            String lastName = lastnameField.getText();

            if (password != confirmPswdField.getText()) { return;}

            boolean success = adminDAO.registerAdmin(firstName, lastName, email, password);
            if (success) {
                System.out.println("Registration successful!");
                App.changeRootScene("/com/bank/views/MainView.fxml", "Bank Dashboard", true);
            } else {
                System.out.println("Registration failed. Email might already be taken.");
            }
        } else {
            // Run the simple, direct credential match verification check
            boolean isValid = adminDAO.authenticateAdmin(email, password);
            
            if (isValid) {
                System.out.println("Login Success! Opening main application...");
                App.changeRootScene("/com/bank/views/MainView.fxml", "Bank Dashboard", true);
            } else {
                System.out.println("Login Failed: Incorrect email or password inputs.");
                // Optional: Update an error message label visually in your UI
            }
        }
}

    //animation

    private void fadeInFields() {
        //Reset initial states
        registerContainer.setOpacity(0.0);
        registerContainer.setTranslateY(-20.0); 

        //Fade Animation
        FadeTransition fade = new FadeTransition(Duration.millis(350), registerContainer);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        //Translate Animation
        TranslateTransition slide = new TranslateTransition(Duration.millis(350), registerContainer);
        slide.setFromY(-20.0);
        slide.setToY(0.0);

        // play both animations
        ParallelTransition combinedAnimation = new ParallelTransition(fade, slide);
        combinedAnimation.play();
    }

}
