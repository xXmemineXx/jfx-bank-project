package com.bank.controller;

import com.bank.dao.ClientDAO;
import com.bank.models.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ClientFormController {

    @FXML private Label formTitle;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField accountField;
    @FXML private TextField balanceField;

    private final ClientDAO clientDAO = new ClientDAO();
    private Client existingClient;
    private boolean isEditMode = false;
    private Runnable onSaveCallback; // Triggers a refresh in the parent view

    /**
     * Configuration for EDIT MODE
     */
    public void setEditMode(Client client, Runnable onSaveCallback) {
        this.isEditMode = true;
        this.existingClient = client;
        this.onSaveCallback = onSaveCallback;

        formTitle.setText("Edit Client Profile");
        
        // Populate inputs using your exact model getter names
        firstNameField.setText(client.get_first_name());
        lastNameField.setText(client.get_last_name());
        emailField.setText(client.get_mail());
        phoneField.setText(client.get_phone());
        accountField.setText(client.get_id());
        balanceField.setText(String.valueOf(client.get_balance()));

        // Safeguard critical account parameters from accidental edits
        accountField.setDisable(true);
        balanceField.setDisable(true);
    }

    /**
     * Configuration for CREATE MODE
     */
    public void setCreateMode(Runnable onSaveCallback) {
        this.isEditMode = false;
        this.onSaveCallback = onSaveCallback;
        formTitle.setText("Register New Client");
        
        // Ensure balance field defaults to a clean string state for inputs
        balanceField.setText("0");
    }

    @FXML
    private void handleSave() {
        // Read input strings safely out of fields
        String fName = firstNameField.getText();
        String lName = lastNameField.getText();
        String mail = emailField.getText();
        String phone = phoneField.getText();

        if (isEditMode) {
            // Apply modifications using your exact setter conventions
            existingClient.set_first_name(fName);
            existingClient.set_last_name(lName);
            existingClient.set_mail(mail);
            existingClient.set_phone(phone);
            
            // TODO: Execute your DAO update method here
            // clientDAO.updateClient(existingClient);
            System.out.println("Updating database entry for client ID: " + existingClient.get_id());
        } else {
            String accId = accountField.getText();
            int initialBalance = Integer.parseInt(balanceField.getText());

            // Build a completely new record instance based on your explicit constructor
            Client newClient = new Client(accId, lName, fName, mail, phone, initialBalance);
            
            // TODO: Execute your DAO insert method here
            // clientDAO.insertClient(newClient);
            System.out.println("Saving new client to database with account number: " + newClient.get_id());
        }

        // Trigger the visual layout refresh callback routine
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) formTitle.getScene().getWindow();
        stage.close();
    }
}
