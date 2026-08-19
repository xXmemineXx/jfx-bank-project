package com.bank.controller;

import com.bank.dao.ClientDAO;
import com.bank.dao.TransferDAO;
import com.bank.models.Client;
import com.bank.models.Transfer;
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


public class TransferFormController {

    @FXML private Label transferLabel;
    @FXML private ComboBox<Client> sender;
    @FXML private ComboBox<Client> receiver;
    @FXML private TextField amount;
    @FXML private Button confirm;
    @FXML private Button cancel;

    private final ClientDAO clientDAO = new ClientDAO();
    private final TransferDAO transferDAO = new TransferDAO();

    private Runnable onSaveCallback;
    private boolean isEditMode = false;
    private Transfer existingTransfer;

    @FXML
    public void initialize() {
        StringConverter<Client> converter = new StringConverter<Client>() {
            @Override
            public String toString(Client c) {
                return c == null ? "" : c.get_id() + " - " + c.get_first_name() + " " + c.get_last_name();
            }
            @Override
            public Client fromString(String string) { return null; }
        };
        sender.setConverter(converter);
        receiver.setConverter(converter);

        cancel.setOnAction(e -> closeWindow());
        confirm.setOnAction(e -> handleConfirm());
    }

    public void setCreateMode(Runnable onSaveCallback) {
        this.isEditMode = false;
        this.onSaveCallback = onSaveCallback;
        transferLabel.setText("New Transfer");

        List<Client> allClients = clientDAO.getAllClients();
        sender.setItems(FXCollections.observableArrayList(allClients));
        receiver.setItems(FXCollections.observableArrayList(allClients));
    }

    /** Édition d'un transfert existant (icône crayon d'une carte "transfer") */
    public void setEditMode(Transfer existingTransfer, Runnable onSaveCallback) {
        this.isEditMode = true;
        this.existingTransfer = existingTransfer;
        this.onSaveCallback = onSaveCallback;
        transferLabel.setText("Edit Transfer");

        Client senderClient = clientDAO.getClient(existingTransfer.get_sender());
        Client receiverClient = clientDAO.getClient(existingTransfer.get_receiver());

        sender.setItems(FXCollections.observableArrayList(senderClient));
        sender.getSelectionModel().select(senderClient);
        sender.setDisable(true);

        receiver.setItems(FXCollections.observableArrayList(receiverClient));
        receiver.getSelectionModel().select(receiverClient);
        receiver.setDisable(true);

        amount.setText(String.valueOf(existingTransfer.get_amount()));
    }

    private void handleConfirm() {
        Client senderClient = sender.getSelectionModel().getSelectedItem();
        Client receiverClient = receiver.getSelectionModel().getSelectedItem();
        String amountText = amount.getText();

        if (senderClient == null || receiverClient == null) {
            showError("Please select both a sender and a receiver.");
            return;
        }
        if (senderClient.get_id().equals(receiverClient.get_id())) {
            showError("Sender and receiver must be different clients.");
            return;
        }
        if (amountText == null || amountText.isBlank()) {
            showError("Please enter an amount.");
            return;
        }

        int transferAmount;
        try {
            transferAmount = Integer.parseInt(amountText.trim());
        } catch (NumberFormatException e) {
            showError("Amount must be a whole number.");
            return;
        }
        if (transferAmount <= 0) {
            showError("Amount must be greater than zero.");
            return;
        }

        boolean success;
        if (isEditMode) {
            existingTransfer.set_amount(transferAmount);
            success = transferDAO.modifier(existingTransfer);
        } else {
            // transfer_id is a SERIAL column - the 0 here is a throwaway placeholder,
            // TransferDAO.ajouter() never reads/inserts it.
            Transfer newTransfer = new Transfer(0, transferAmount,
                senderClient.get_id(), senderClient.get_first_name(),
                receiverClient.get_id(), receiverClient.get_first_name(),
                LocalDateTime.now());
            success = transferDAO.ajouter(newTransfer);
        }

        if (success) {
            if (onSaveCallback != null) onSaveCallback.run();
            closeWindow();
        } else {
            showError(isEditMode
                ? "Could not update the transfer."
                : "Could not create the transfer. Check the sender's balance and try again.");
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
