package com.bank.controller;

import com.bank.models.Transfer;
import com.bank.dao.ClientDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.time.format.DateTimeFormatter;

public class TransferPdfController {
    private ClientDAO clientdao = new ClientDAO();

    @FXML private Label dateLabel;
    @FXML private Label transferIdLabel;
    @FXML private Label senderAccLabel;
    @FXML private Label senderNameLabel;
    @FXML private Label receiverAccLabel;
    @FXML private Label receiverNameLabel;
    @FXML private Label amountLabel;
    @FXML private Label senderBalance;

    public void initData(Transfer transfer) {
        // Formatage de la date (Ex: 19-08-2026 10:15)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        
        // Liaison directe avec tes getters exacts du modèle Transfer
        dateLabel.setText("date : " + transfer.get_date().format(formatter));
        transferIdLabel.setText("virement num : #" + transfer.get_id());
        
        senderAccLabel.setText(transfer.get_sender());
        senderNameLabel.setText(transfer.get_sender_name());
        
        receiverAccLabel.setText(transfer.get_receiver());
        receiverNameLabel.setText(transfer.get_receiver_name());

        senderBalance.setText(String.format("%,d Ar", clientdao.get_client_balance(transfer.get_sender())));
        
        // Formatage du montant avec la devise locale (Ex: 50 000 Ar)
        amountLabel.setText(String.format("%,d Ar", transfer.get_amount()));
    }
}
