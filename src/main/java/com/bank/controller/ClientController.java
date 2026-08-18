package com.bank.controller;

import com.bank.dao.ClientDAO;
import com.bank.models.Client;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.List;

public class ClientController {

    @FXML
    private FlowPane cardsGrid; // Injected from Scene Builder

    private final ClientDAO clientDAO = new ClientDAO();

    @FXML
    public void initialize() {
        loadClientCards();
    }

    private void loadClientCards() {
        // 1. Clear out any dummy template data from the grid
        cardsGrid.getChildren().clear();

        // 2. Fetch fresh live records from PostgreSQL
        List<Client> clients = clientDAO.getAllClients();

        // 3. Loop through every client record
        for (Client client : clients) {
            try {
                // Load a fresh visual instance of your card template FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bank/views/cards.fxml"));
                
                // Keep the root casting matching your card template's root container type
                VBox card = loader.load(); 

                // 4. Pass the client data straight to the card's individual controller
                CardController cardController = loader.getController();
                cardController.setClientData(client);

                // 5. Inject this fully loaded card layout directly into your responsive grid
                cardsGrid.getChildren().add(card);

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to load ClientCard.fxml template.");
            }
        }

        
    }
}
