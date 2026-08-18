package com.bank.controller;

import com.bank.helpers.ActionCard;
import com.bank.models.Transfer; 
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class TransferController implements ActionCard {
    @FXML private Label senderName;
    @FXML private Label receiverName;
    @FXML private Label transferDateLabel;
    @FXML private Label transferedAmountLabel;


    @Override
    public void populateCardData(Object dataRecord) {
        // Cast the generic object to your specific model type
        Transfer transfer = (Transfer) dataRecord; 
        
        senderName.setText("sender : " + transfer.get_sender_name());
        receiverName.setText("receiver : " + transfer.get_receiver_name());
        transferDateLabel.setText(transfer.get_date().toString());
        transferedAmountLabel.setText(String.valueOf(transfer.get_amount()));
    }
}
