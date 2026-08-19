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
import javafx.scene.control.TextField;

public class ClientController {

    @FXML private FlowPane cardsGrid;
    @FXML private StackPane infoSlot;
    @FXML private Label totalClients;
    @FXML private TextField searchField;

    private final ClientDAO clientDAO = new ClientDAO();
    private String selectedClientId = null; 

    @FXML
    public void initialize() {
        loadFilteredClientCards(""); 

        //ontinuous real-time typing listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadFilteredClientCards(newValue);
        });
        
        //real-time background tracking sync loop
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            System.out.println("Auto-refreshing view from database...");
            
            // FIX 1: Pass the current live search input instead of a blank string "" 
            // This prevents your active search grid from breaking every 3 seconds!
            loadFilteredClientCards(searchField.getText());           
               
            if (selectedClientId != null) {
                // FIX 2: Corrected method name from 'getClient' to 'getClientById'
                Client freshClientData = clientDAO.getClient(selectedClientId);
                if (freshClientData != null) {
                    showDetailedProfile(freshClientData);
                }
            }
        }));
        
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play(); 
    }

    
    // Unified card grid processing

    private void loadFilteredClientCards(String filterText) {
        cardsGrid.getChildren().clear();

        List<Client> clients;
        
        if (filterText == null || filterText.trim().isEmpty()) {
            clients = clientDAO.getAllClients();
        } else {
            clients = clientDAO.searchClients(filterText);
        }

        if (totalClients != null) {
            totalClients.setText(String.valueOf(clients.size()));
        }

        for (Client client : clients) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bank/views/cards.fxml"));
                VBox card = loader.load();

                CardController cardController = loader.getController();
                cardController.setClientData(client, 
                    selectedClient -> {
                        this.selectedClientId = selectedClient.get_id();
                        showDetailedProfile(selectedClient);
                    }, 
                    editedClient -> {
                        editClient(editedClient);
                    }
                );

                cardsGrid.getChildren().add(card);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleResetSearch() {
        searchField.clear(); 
    }

    @FXML
    private void addClient() {
        openFormModal(null); 
    }

    @FXML
    private void handleShortcutLoan() {
        // Vérifie si un client est sélectionné (l'ID stocké dans ta variable globale de classe)
        if (selectedClientId == null) {
            System.out.println("Aucun client sélectionné pour le raccourci !");
            return;
        }

        // Récupérer l'objet client complet
        Client activeClient = clientDAO.getClient(selectedClientId);
        if (activeClient == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bank/views/loanForm.fxml"));
            Parent root = loader.load();

            LoanFormController formController = loader.getController();
            // Mode raccourci : transmet le client et verrouille le champ
            formController.setShortcutMode(activeClient, () -> {
                // Callback de rafraîchissement (recharger tes listes si nécessaire)
                loadFilteredClientCards(searchField.getText());
            });

            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle("Nouveau Prêt");
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleShortcutReturn() {
        if (selectedClientId == null) return;

        Client activeClient = clientDAO.getClient(selectedClientId);
        if (activeClient == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bank/views/returnForm.fxml"));
            Parent root = loader.load();

            ReturnFormController formController = loader.getController();
            // Configure ton contrôleur Return de la même manière pour bloquer le client
            formController.setShortcutMode(activeClient, () -> {
                loadFilteredClientCards(searchField.getText());
            });

            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle("Retour de Prêt");
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
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

    public void editClient(Client client) {
        openFormModal(client); 
    }

    private void openFormModal(Client clientToLoad) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bank/views/clientForm.fxml"));
            Parent root = loader.load();

            ClientFormController formController = loader.getController();
            
            // FIX 3: Point the layout refresh handlers to 'triggerCurrentRefresh' instead of missing method calls
            if (clientToLoad != null) {
                formController.setEditMode(clientToLoad, this::triggerCurrentRefresh);
            } else {
                formController.setCreateMode(this::triggerCurrentRefresh);
            }

            Stage modalStage = new Stage();
            modalStage.setTitle(clientToLoad == null ? "New Client Profile" : "Modify Client Profile");
            modalStage.initModality(Modality.APPLICATION_MODAL);
            
            Stage primaryWindow = (Stage) cardsGrid.getScene().getWindow();
            modalStage.initOwner(primaryWindow);

            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false); 
            modalStage.showAndWait();       

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not load clientForm.fxml view sheet template file.");
        }
    }

    private void triggerCurrentRefresh() {
        loadFilteredClientCards(searchField.getText());
    }
}
