package com.bank.controller;

import com.bank.dao.ReturnsDAO;
import com.bank.helpers.ActionCard;
import com.bank.models.Returns;
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

public class ReturnController implements ActionCard {
    @FXML private HBox cardRoot;
    @FXML private Label loanIdLabel;
    @FXML private Label returnIdLabel;
    @FXML private Label debtorNameLabel;
    @FXML private Label returnDateLabel;
    @FXML private Label returnedAmountLabel;
    @FXML private Label loanAmountLabel;
    @FXML private Label unpayedAmountLabel;
    @FXML private Label statusLabel;

    private final ReturnsDAO returnsDAO = new ReturnsDAO();
    private Returns currentReturn;

    @Override
    public void populateCardData(Object dataRecord) {
        // Cast the generic object to your specific model type
        Returns ret = (Returns) dataRecord;
        this.currentReturn = ret;

        debtorNameLabel.setText(ret.get_debtor());
        loanIdLabel.setText("loan n : " + ret.get_loan());
        unpayedAmountLabel.setText(String.valueOf(ret.get_unpayed()));
        returnIdLabel.setText(ret.get_id());
        returnDateLabel.setText(ret.get_date().toString());
        returnedAmountLabel.setText(String.valueOf(ret.get_amount()));
        loanAmountLabel.setText(String.valueOf(ret.get_loan_amount()));
        statusLabel.setText("actual status : " + ret.get_status());
    }

    // trash icon
    @FXML
    private void handleDelete() {
        if (currentReturn == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete this repayment record?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> answer = confirm.showAndWait();

        if (answer.isPresent() && answer.get() == ButtonType.YES) {
            boolean success = returnsDAO.supprimer(currentReturn.get_id());
            if (success && cardRoot.getParent() instanceof Pane parent) {
                parent.getChildren().remove(cardRoot);
            } else if (!success) {
                new Alert(Alert.AlertType.ERROR, "Could not delete this repayment.").showAndWait();
            }
        }
    }

    // pencil icon
    @FXML
    private void handleEdit() {
        if (currentReturn == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bank/views/returnForm.fxml"));
            Parent root = loader.load();

            ReturnFormController formController = loader.getController();
            formController.setEditMode(currentReturn, () -> populateCardData(currentReturn));

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Edit Repayment");
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
