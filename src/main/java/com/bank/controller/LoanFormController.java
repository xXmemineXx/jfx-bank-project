package com.bank.controller;

import com.bank.dao.ClientDAO;
import com.bank.dao.LoansDAO;
import com.bank.models.Client;
import com.bank.models.Loans;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.time.LocalDateTime;
import java.util.List;

public class LoanFormController {

    @FXML private Label loanLabel;
    @FXML private ComboBox<Client> client; // Typé avec ton modèle Client
    @FXML private TextField amount;
    @FXML private Button conofirm; // Conserve ton orthographe FXML
    @FXML private Button cancel;

    private final ClientDAO clientDAO = new ClientDAO();
    private final LoansDAO loanDAO = new LoansDAO();
    private Runnable onSaveCallback;

    private boolean isEditMode = false;
    private Loans existingLoan;

    @FXML
    public void initialize() {
        // Définir comment afficher le client dans le ComboBox
        client.setConverter(new StringConverter<Client>() {
            @Override
            public String toString(Client c) {
                return c == null ? "" : c.get_id() + " - " + c.get_first_name() + " " + c.get_last_name();
            }
            @Override
            public Client fromString(String string) { return null; }
        });

        cancel.setOnAction(e -> closeWindow());
        conofirm.setOnAction(e -> handleConfirm());
    }

    /**
     * APPEL VIA MENU GÉNÉRAL : Charge tous les clients
     */
    public void setCreateMode(Runnable onSaveCallback) {
        this.isEditMode = false;
        this.onSaveCallback = onSaveCallback;
        loanLabel.setText("New Loan");
        List<Client> allClients = clientDAO.getAllClients();
        client.setItems(FXCollections.observableArrayList(allClients));
    }

    /**
     * APPEL VIA RACCOURCI : Verrouille le client sélectionné
     */
    public void setShortcutMode(Client selectedClient, Runnable onSaveCallback) {
        this.isEditMode = false;
        this.onSaveCallback = onSaveCallback;
        loanLabel.setText("New Loan");
        client.setItems(FXCollections.observableArrayList(selectedClient));
        client.getSelectionModel().select(selectedClient);
        client.setDisable(true); // Rend le choix immuable
    }

    /**
     * Édition d'un prêt existant (déclenché depuis l'icône crayon d'une carte)
     */
    public void setEditMode(Loans existingLoan, Runnable onSaveCallback) {
        this.isEditMode = true;
        this.existingLoan = existingLoan;
        this.onSaveCallback = onSaveCallback;
        loanLabel.setText("Edit Loan");

        Client debtor = clientDAO.getClient(existingLoan.get_debtor());
        client.setItems(FXCollections.observableArrayList(debtor));
        client.getSelectionModel().select(debtor);
        client.setDisable(true); // le débiteur d'un prêt ne se change pas après coup

        amount.setText(String.valueOf(existingLoan.get_amount()));
    }

    private void handleConfirm() {
        Client targetClient = client.getSelectionModel().getSelectedItem();
        String loanAmountText = amount.getText();

        if (targetClient == null) {
            showError("Please select a client.");
            return;
        }
        if (loanAmountText == null || loanAmountText.isBlank()) {
            showError("Please enter a loan amount.");
            return;
        }

        int loanAmount;
        try {
            loanAmount = Integer.parseInt(loanAmountText.trim());
        } catch (NumberFormatException e) {
            showError("Amount must be a whole number.");
            return;
        }
        if (loanAmount <= 0) {
            showError("Amount must be greater than zero.");
            return;
        }

        boolean success;
        if (isEditMode) {
            existingLoan.set_amount(loanAmount);
            success = loanDAO.modifier(existingLoan);
        } else {
            // varchar(10) primary key - timestamp-derived id keeps it short and unique enough
            String loanId = "L" + (System.currentTimeMillis() % 100000000L);
            Loans newLoan = new Loans(loanAmount, loanId, targetClient.get_id(),
                targetClient.get_first_name(), LocalDateTime.now());
            success = loanDAO.ajouter(newLoan);
        }

        if (success) {
            if (onSaveCallback != null) onSaveCallback.run();
            closeWindow();
        } else {
            showError(isEditMode
                ? "Could not update the loan."
                : "Could not create the loan. This client may already have an active unpaid loan.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText("Operation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancel.getScene().getWindow();
        stage.close();
    }
}
