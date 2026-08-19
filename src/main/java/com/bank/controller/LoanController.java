package com.bank.controller;

import java.time.format.DateTimeFormatter;

import com.bank.dao.LoansDAO;
import com.bank.helpers.ActionCard;
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
import java.util.Optional;

public class LoanController implements ActionCard {
    @FXML private HBox cardRoot;
    @FXML private Label debtorName;
    @FXML private Label amountLabel;
    @FXML private Label dateLabel;
    @FXML private Label idLabel;
    @FXML private Label incomeLabel;

    private final LoansDAO loanDAO = new LoansDAO();
    private Loans currentLoan;

    @Override
    public void populateCardData(Object dataRecord) {
        // Cast the generic object to your specific model type
        Loans loan = (Loans) dataRecord;
        this.currentLoan = loan;

        debtorName.setText(loan.get_debtor_name());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        dateLabel.setText(loan.get_date().format(formatter));
        idLabel.setText(loan.get_id());
        incomeLabel.setText(String.valueOf(loan.get_amount() * 0.1));
        amountLabel.setText(String.valueOf(loan.get_amount()));
    }

    // trash icon
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

    // pencil icon
    @FXML
    private void handleEdit() {
        if (currentLoan == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bank/views/loanForm.fxml"));
            Parent root = loader.load();

            LoanFormController formController = loader.getController();
            formController.setEditMode(currentLoan, () -> {
                // Refreshed loan comes back through the periodic auto-refresh loop;
                // re-populate this card immediately too for instant feedback.
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
}
