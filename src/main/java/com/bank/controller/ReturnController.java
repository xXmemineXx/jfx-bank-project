package com.bank.controller;

import com.bank.helpers.ActionCard;
import com.bank.models.Returns; 
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ReturnController implements ActionCard {
    @FXML private Label loanIdLabel;
    @FXML private Label returnIdLabel;
    @FXML private Label debtorNameLabel;
    @FXML private Label returnDateLabel;
    @FXML private Label returnedAmountLabel;
    @FXML private Label loanAmountLabel;
    @FXML private Label unpayedAmountLabel;
    @FXML private Label statusLabel;


    @Override
    public void populateCardData(Object dataRecord) {
        // Cast the generic object to your specific model type
        Returns ret = (Returns) dataRecord; 
        
        debtorNameLabel.setText(ret.get_debtor());
        loanIdLabel.setText("loan n : " + ret.get_loan());
        unpayedAmountLabel.setText(String.valueOf(ret.get_unpayed()));
        returnIdLabel.setText(ret.get_id());
        returnDateLabel.setText(ret.get_date().toString());
        returnedAmountLabel.setText(String.valueOf(ret.get_amount()));
        loanAmountLabel.setText(String.valueOf(ret.get_loan_amount()));
        statusLabel.setText("actual status : " +ret.get_status());
    }
}
