package com.bank.controller;

//this card is only for the client
//sorry for the naming but i don't have time to rename everything
//related to it

import com.bank.models.Client;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class CardController {

    private Client client;
    private Consumer<Client> onCardView;
    private Consumer<Client> onCardEdit;
    private Consumer<Client> onCardDelete;

    @FXML
    private Label type;
    @FXML
    private Label description;


    public void setClientData(Client client,
                            Consumer<Client> viewCallback,
                            Consumer<Client> editCallback,
                            Consumer<Client> deleteCallback) {
        this.client = client;
        this.onCardView = viewCallback;
        this.onCardEdit = editCallback;
        this.onCardDelete = deleteCallback;

        type.setText("account n: " + client.get_id());
        description.setText(client.get_first_name() + " " + client.get_last_name());
    }

    @FXML
    public void deleteCardClick(javafx.event.ActionEvent event)
    {
        event.consume();
        if (onCardDelete != null) {
            onCardDelete.accept(client);
        }
    }

    @FXML
    public void viewCardClick(javafx.event.ActionEvent event)
    {
        event.consume();
        if (onCardEdit != null) {
            onCardEdit.accept(client);
        }
    }

    @FXML
    public void editCardClick(javafx.event.ActionEvent event)
    {
        event.consume();
        if (onCardView != null) {
            onCardView.accept(client);
        }
    }
}
