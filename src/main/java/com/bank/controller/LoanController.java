package com.bank.controller;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.bank.dao.ClientDAO;
import com.bank.dao.LoansDAO;
import com.bank.helpers.ActionCard;
import com.bank.helpers.MailService;
import com.bank.models.Client;
import com.bank.models.Loans;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class LoanController implements ActionCard {
    @FXML private HBox cardRoot;
    @FXML private Label debtorName;
    @FXML private Label amountLabel;
    @FXML private Label dateLabel;
    @FXML private Label idLabel;
    @FXML private Label incomeLabel;

    private final LoansDAO loanDAO = new LoansDAO();
    private final ClientDAO clientDAO = new ClientDAO();
    private Loans currentLoan;

    // Lazily created so missing mail.properties does not break the whole card grid
    private MailService mailService;

    @Override
    public void populateCardData(Object dataRecord) {
        Loans loan = (Loans) dataRecord;
        this.currentLoan = loan;

        debtorName.setText(loan.get_debtor_name());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        dateLabel.setText(loan.get_date().format(formatter));
        idLabel.setText(loan.get_id());
        incomeLabel.setText(String.valueOf(loan.get_amount() * 0.1));
        amountLabel.setText(String.valueOf(loan.get_amount()));
    }

    /** Mailbox icon — send a templated reminder to the debtor. */
    @FXML
    private void handleSendMail() {
        if (currentLoan == null) return;

        Client client = clientDAO.getClient(currentLoan.get_debtor());
        if (client == null) {
            showError("Client not found for this loan.");
            return;
        }

        String email = client.get_mail();
        if (email == null || email.isBlank()) {
            showError("This client has no email address on file.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Send a loan reminder to " + client.get_first_name() + " <" + email + ">?",
            ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.YES) return;

        // Build template variables
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        Map<String, String> vars = new HashMap<>();
        vars.put("client_name", client.get_first_name() + " " + client.get_last_name());
        vars.put("loan_id", currentLoan.get_id());
        vars.put("amount", String.valueOf(currentLoan.get_amount()));
        vars.put("loan_date", currentLoan.get_date().format(formatter));
        vars.put("account_id", client.get_id());
        vars.put("bank_name", "Your Bank");

        String subject = MailService.applyTemplate(MailService.LOAN_REMINDER_SUBJECT, vars);
        String body = MailService.applyTemplate(MailService.LOAN_REMINDER_BODY, vars);

        try {
            if (mailService == null) {
                mailService = new MailService();
            }
        } catch (Exception e) {
            showError("Mail is not configured. Check src/main/resources/mail.properties\n" + e.getMessage());
            return;
        }

        // Disable further clicks while sending (optional visual feedback)
        cardRoot.setDisable(true);

        mailService.sendAsync(email, subject, body,
            () -> {
                cardRoot.setDisable(false);
                new Alert(Alert.AlertType.INFORMATION,
                    "Reminder sent to " + email).showAndWait();
            },
            err -> {
                cardRoot.setDisable(false);
                showError("Failed to send email:\n" + err);
            }
        );
    }

    @FXML
    private void handleDelete() {
        if (currentLoan == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete loan " + currentLoan.get_id() + "?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> answer = confirm.showAndWait();

        if (answer.isPresent() && answer.get() == ButtonType.YES) {
            boolean success = loanDAO.supprimer(currentLoan.get_id());
            if (success && cardRoot.getParent() instanceof Pane parent) {
                parent.getChildren().remove(cardRoot);
            } else if (!success) {
                new Alert(Alert.AlertType.ERROR, "Could not delete this loan.").showAndWait();
            }
        }
    }

    @FXML
    private void handleEdit() {
        if (currentLoan == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bank/views/loanForm.fxml"));
            Parent root = loader.load();

            LoanFormController formController = loader.getController();
            formController.setEditMode(currentLoan, () -> {
                Loans updated = loanDAO.getActiveLoanForClient(currentLoan.get_debtor());
                if (updated != null) populateCardData(updated);
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Edit Loan");
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
