package com.bank.controller;

import com.bank.models.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ClientInfoController {

    @FXML private Label idLabel;
    @FXML private Label fNameLabel;
    @FXML private Label lNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label balanceLabel;

    public void initData(Client client) {
        idLabel.setText("client id :" + client.get_id());
        fNameLabel.setText(client.get_first_name());
        lNameLabel.setText(client.get_last_name());
        emailLabel.setText(client.get_mail());
        phoneLabel.setText(client.get_phone());
        balanceLabel.setText("Ar" + String.format("%.2f", client.get_balance()));
    }
}
