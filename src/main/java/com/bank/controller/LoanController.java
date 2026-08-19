package com.bank.controller;

import java.time.format.DateTimeFormatter;

import com.bank.helpers.ActionCard;
import com.bank.models.Loans; 
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class LoanController implements ActionCard {
    @FXML private Label debtorName;
    @FXML private Label amountLabel;
    @FXML private Label dateLabel;
    @FXML private Label idLabel;
    @FXML private Label incomeLabel;


    @Override
    public void populateCardData(Object dataRecord) {
        // Cast the generic object to your specific model type
        Loans loan = (Loans) dataRecord; 
        
        debtorName.setText(loan.get_debtor_name());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        dateLabel.setText(loan.get_date().format(formatter));
        idLabel.setText(loan.get_id());
        incomeLabel.setText(String.valueOf(loan.get_amount() * 0.1));
        amountLabel.setText(String.valueOf(loan.get_amount()));
    }
}
