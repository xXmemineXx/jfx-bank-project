package com.bank.controller;

import com.bank.dao.ClientDAO;
import com.bank.dao.LoansDAO;
import com.bank.dao.ReturnsDAO;
import com.bank.models.Client;
import com.bank.models.Loans;
import com.bank.models.Returns;
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

public class ReturnFormController {

    @FXML private Label returnLabel;
    @FXML private ComboBox<Client> clientName;
    @FXML private Label loanIdLabel;
    @FXML private Label loanAmountLabel;
    @FXML private TextField amount;
    @FXML private Button confirm;
    @FXML private Button cancel;

    private final ClientDAO clientDAO = new ClientDAO();
    private final LoansDAO loanDAO = new LoansDAO();
    private final ReturnsDAO returnsDAO = new ReturnsDAO();

    private Runnable onSaveCallback;
    private Loans activeLoan;

    private boolean isEditMode = false;
    private Returns existingReturn;

    @FXML
    public void initialize() {
        clientName.setConverter(new StringConverter<Client>() {
            @Override
            public String toString(Client c) {
                return c == null ? "" : c.get_id() + " - " + c.get_first_name() + " " + c.get_last_name();
            }
            @Override
            public Client fromString(String string) { return null; }
        });

        // As soon as a client is picked, look up the loan they're actually repaying
        clientName.valueProperty().addListener((obs, oldClient, newClient) -> loadActiveLoan(newClient));

        cancel.setOnAction(e -> closeWindow());
        confirm.setOnAction(e -> handleConfirm());
    }

    /** APPEL VIA MENU GÉNÉRAL : Charge tous les clients */
    public void setCreateMode(Runnable onSaveCallback) {
        this.isEditMode = false;
        this.onSaveCallback = onSaveCallback;
        returnLabel.setText("New Loan Repayment");

        List<Client> allClients = clientDAO.getAllClients();
        clientName.setItems(FXCollections.observableArrayList(allClients));
    }

    /** APPEL VIA RACCOURCI : Verrouille le client sélectionné */
    public void setShortcutMode(Client selectedClient, Runnable onSaveCallback) {
        this.isEditMode = false;
        this.onSaveCallback = onSaveCallback;
        returnLabel.setText("New Loan Repayment");

        clientName.setItems(FXCollections.observableArrayList(selectedClient));
        clientName.getSelectionModel().select(selectedClient);
        clientName.setDisable(true);
        loadActiveLoan(selectedClient);
    }

    /** Édition d'un remboursement existant (icône crayon d'une carte "return") */
    public void setEditMode(Returns existingReturn, Runnable onSaveCallback) {
        this.isEditMode = true;
        this.existingReturn = existingReturn;
        this.onSaveCallback = onSaveCallback;
        returnLabel.setText("Edit Repayment");

        loanIdLabel.setText(existingReturn.get_loan());
        // Show how much is still owed if this repayment were removed, so the user
        // knows the maximum they may set for this row.
        int room = returnsDAO.getOutstandingAmountExcluding(
            existingReturn.get_loan(),
            existingReturn.get_loan_amount(),
            existingReturn.get_id()
        );
        loanAmountLabel.setText(String.valueOf(room));
        amount.setText(String.valueOf(existingReturn.get_amount()));

        clientName.setDisable(true);
        clientName.setPromptText(existingReturn.get_debtor());
    }

    private void loadActiveLoan(Client selectedClient) {
        activeLoan = null;
        loanIdLabel.setText("-");
        loanAmountLabel.setText("-");

        if (selectedClient == null) return;

        activeLoan = loanDAO.getActiveLoanForClient(selectedClient.get_id());
        if (activeLoan != null) {
            // Label shows the *remaining* balance, not the original loan total
            int outstanding = returnsDAO.getOutstandingAmount(activeLoan.get_id(), activeLoan.get_amount());
            loanIdLabel.setText(activeLoan.get_id());
            loanAmountLabel.setText(String.valueOf(outstanding));
        } else {
            loanIdLabel.setText("no active loan");
        }
    }

    private void handleConfirm() {
        String amountText = amount.getText();
        if (amountText == null || amountText.isBlank()) {
            showError("Please enter a repayment amount.");
            return;
        }

        int repaymentAmount;
        try {
            repaymentAmount = Integer.parseInt(amountText.trim());
        } catch (NumberFormatException e) {
            showError("Amount must be a whole number.");
            return;
        }
        if (repaymentAmount <= 0) {
            showError("Amount must be greater than zero.");
            return;
        }

        boolean success;

        if (isEditMode) {
            // Cap against outstanding *excluding* this row so previous partial
            // payments are still counted.
            int room = returnsDAO.getOutstandingAmountExcluding(
                existingReturn.get_loan(),
                existingReturn.get_loan_amount(),
                existingReturn.get_id()
            );
            if (repaymentAmount > room) {
                showError("Repayment exceeds the outstanding loan balance of " + room + ".");
                return;
            }

            int remaining = room - repaymentAmount;
            boolean fullyRepaid = remaining == 0;

            existingReturn.set_amount(repaymentAmount);
            existingReturn.set_unpayed(remaining);
            existingReturn.set_fully_returned(fullyRepaid);

            success = returnsDAO.modifier(existingReturn);
        } else {
            if (activeLoan == null) {
                showError("This client has no active loan to repay.");
                return;
            }

            // Sum of *previous* repayments is subtracted from the original loan.
            int outstanding = returnsDAO.getOutstandingAmount(activeLoan.get_id(), activeLoan.get_amount());
            if (repaymentAmount > outstanding) {
                showError("Repayment exceeds the outstanding loan balance of " + outstanding + ".");
                return;
            }

            // varchar(10) primary key - timestamp-derived id keeps it short and unique enough
            String returnId = "R" + (System.currentTimeMillis() % 100000000L);
            int remaining = outstanding - repaymentAmount;
            boolean fullyRepaid = remaining == 0;

            Returns newReturn = new Returns(
                fullyRepaid ? "fully returned" : "still in debt",
                activeLoan.get_amount(),
                repaymentAmount,
                remaining,
                returnId,
                activeLoan.get_id(),
                activeLoan.get_debtor_name(),
                LocalDateTime.now(),
                fullyRepaid
            );
            success = returnsDAO.ajouter(newReturn);
        }

        if (success) {
            if (onSaveCallback != null) onSaveCallback.run();
            closeWindow();
        } else {
            showError(isEditMode
                ? "Could not update the repayment."
                : "Could not record the repayment. It may exceed the outstanding loan balance.");
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
