package com.bank.controller;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import com.bank.dao.ClientDAO;
import com.bank.models.Client;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.List;
import javafx.scene.control.Label;

public class ClientController {

    @FXML private FlowPane cardsGrid;
    @FXML private StackPane infoSlot;
    @FXML private Label totalClients;

    private final ClientDAO clientDAO = new ClientDAO();
    
    // FIX: Changed from int to String, initialized to null
    private String selectedClientId = null; 

    @FXML
    private void addClient() {
        openFormModal(null); // Passing null flags creation mode
    }

    @FXML
    public void initialize() {
        loadClientCards();
        
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            System.out.println("Auto-refreshing view from database...");
            loadClientCards(); 
            
               
            if (selectedClientId != null) {
                Client freshClientData = clientDAO.getClient(selectedClientId);
                if (freshClientData != null) {
                    showDetailedProfile(freshClientData);
                }
            }
        }));
        
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play(); 
    }

    private void loadClientCards() {
        // Clear out any dummy template data from the grid
        cardsGrid.getChildren().clear();

        // Fetch fresh live records from PostgreSQL
        List<Client> clients = clientDAO.getAllClients();
        
        totalClients.setText(String.valueOf(clients.size()));

        // Loop through every client record
        for (Client client : clients) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bank/views/cards.fxml"));
                VBox card = loader.load(); 

                CardController cardController = loader.getController();
                cardController.setClientData(client, 
                                            selectedClient -> {
                                                // FIX: Save the String ID cleanly on click
                                                this.selectedClientId = selectedClient.get_id(); 
                                                showDetailedProfile(selectedClient);
                                            }, editedClient -> {
                                                editClient(editedClient);
                                            });

                cardsGrid.getChildren().add(card);

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to load cards.fxml template.");
            }
        }
    }

    public void showDetailedProfile(Client client) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bank/views/clientInfo.fxml"));
            Parent infoView = loader.load();

            ClientInfoController controller = loader.getController();
            controller.initData(client);

            infoSlot.getChildren().clear();
            infoSlot.getChildren().add(infoView);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not load clientInfo.fxml file.");
        }
    }

    /**
     * Connected to your CardController's edit callback routing slot
     */
    public void editClient(Client client) {
        openFormModal(client); // Passing an object flags edit mode
    }

    private void openFormModal(Client clientToLoad) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bank/views/clientForm.fxml"));
            Parent root = loader.load();

            ClientFormController formController = loader.getController();
            
            // Configure form state mode dynamically using our Runnable callback interface
            if (clientToLoad != null) {
                formController.setEditMode(clientToLoad, this::loadClientCards);
            } else {
                formController.setCreateMode(this::loadClientCards);
            }

            // Initialize and bind the Modal Window Properties
            Stage modalStage = new Stage();
            modalStage.setTitle(clientToLoad == null ? "New Client Profile" : "Modify Client Profile");
            
            // CRUCIAL ACTIONS: Block interaction with the background window layout fields
            modalStage.initModality(Modality.APPLICATION_MODAL);
            
            // Ensure the pop-up sits on top of your current active application viewport window frame
            Stage primaryWindow = (Stage) cardsGrid.getScene().getWindow();
            modalStage.initOwner(primaryWindow);

            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false); // Prevents users from breaking form proportions
            modalStage.showAndWait();       // Freezes code execution on this line until closed

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not load clientForm.fxml view sheet template file.");
        }
    }
}
