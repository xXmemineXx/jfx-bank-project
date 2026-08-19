package com.bank.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import com.bank.helpers.ActionCard;
import com.bank.dao.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ActionsController {

    private String currentViewPath = "/com/bank/views/loan.fxml";

    /** null = all, Boolean.TRUE = payed, Boolean.FALSE = unpayed (returns view only) */
    private Boolean returnsFilter = null;

    @FXML private VBox actionCardsGrid;
    @FXML private TextField searchField;

    private final LoansDAO loanDAO = new LoansDAO();
    private final TransferDAO transferDAO = new TransferDAO();
    private final ReturnsDAO returnsDAO = new ReturnsDAO();

    @FXML
    public void initialize() {
        // Live typing listener – same pattern as ClientController
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                applySearchAndFilter(newValue);
            });
        }

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            System.out.println("Auto-refreshing view from database...");
            refreshCurrentView();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        viewLoans();
    }

    // ── sidebar buttons ──────────────────────────────────────────────

    @FXML
    private void viewLoans() {
        currentViewPath = "/com/bank/views/loan.fxml";
        returnsFilter = null; // filter only applies to returned loans
        if (searchField != null) searchField.clear();
        loadDynamicContent(currentViewPath, loanDAO.getAllLoans());
    }

    @FXML
    private void viewTransfers() {
        currentViewPath = "/com/bank/views/transfer.fxml";
        returnsFilter = null;
        if (searchField != null) searchField.clear();
        loadDynamicContent(currentViewPath, transferDAO.getAllTransfers());
    }

    @FXML
    private void viewReturns() {
        currentViewPath = "/com/bank/views/return.fxml";
        returnsFilter = null;
        if (searchField != null) searchField.clear();
        loadDynamicContent(currentViewPath, returnsDAO.getAllReturns());
    }

    // ── search / reset ───────────────────────────────────────────────

    @FXML
    private void handleResetSearch() {
        if (searchField != null) {
            searchField.clear();
        }
        // clearing already triggers the listener which reloads everything
    }

    // ── filter menu (returned loans only) ────────────────────────────

    @FXML
    private void filterAll() {
        returnsFilter = null;
        applySearchAndFilter(searchField != null ? searchField.getText() : "");
    }

    @FXML
    private void filterPayed() {
        // only meaningful on the returns view – switch to it if needed
        if (!currentViewPath.contains("return")) {
            currentViewPath = "/com/bank/views/return.fxml";
        }
        returnsFilter = Boolean.TRUE;
        applySearchAndFilter(searchField != null ? searchField.getText() : "");
    }

    @FXML
    private void filterUnpayed() {
        if (!currentViewPath.contains("return")) {
            currentViewPath = "/com/bank/views/return.fxml";
        }
        returnsFilter = Boolean.FALSE;
        applySearchAndFilter(searchField != null ? searchField.getText() : "");
    }

    // ── add button ───────────────────────────────────────────────────

    @FXML
    private void addNew() {
        try {
            String formPath;
            if (currentViewPath.contains("loan")) {
                formPath = "/com/bank/views/loanForm.fxml";
            } else if (currentViewPath.contains("transfer")) {
                formPath = "/com/bank/views/transferForm.fxml";
            } else {
                formPath = "/com/bank/views/returnForm.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(formPath));
            Parent root = loader.load();
            Object controller = loader.getController();

            if (controller instanceof LoanFormController lfc) {
                lfc.setCreateMode(this::refreshCurrentView);
            } else if (controller instanceof TransferFormController tfc) {
                tfc.setCreateMode(this::refreshCurrentView);
            } else if (controller instanceof ReturnFormController rfc) {
                rfc.setCreateMode(this::refreshCurrentView);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not load add-form for path: " + currentViewPath);
        }
    }

    // ── core loading helpers ─────────────────────────────────────────

    private void applySearchAndFilter(String filterText) {
        List<?> data;

        if (currentViewPath.contains("loan")) {
            if (filterText == null || filterText.trim().isEmpty()) {
                data = loanDAO.getAllLoans();
            } else {
                data = loanDAO.searchLoans(filterText);
            }
        } else if (currentViewPath.contains("transfer")) {
            if (filterText == null || filterText.trim().isEmpty()) {
                data = transferDAO.getAllTransfers();
            } else {
                data = transferDAO.searchTransfers(filterText);
            }
        } else { // returns
            boolean hasSearch = filterText != null && !filterText.trim().isEmpty();
            if (returnsFilter == null) {
                data = hasSearch ? returnsDAO.searchReturns(filterText) : returnsDAO.getAllReturns();
            } else {
                data = hasSearch
                    ? returnsDAO.searchReturnsBySituation(filterText, returnsFilter)
                    : returnsDAO.listerParSituation(returnsFilter);
            }
        }

        loadDynamicContent(currentViewPath, data);
    }

    private void loadDynamicContent(String fxmlPath, List<?> dataList) {
        actionCardsGrid.getChildren().clear();

        for (Object item : dataList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Node UIElement = loader.load();

                ActionCard cardController = loader.getController();
                cardController.populateCardData(item);

                actionCardsGrid.getChildren().add(UIElement);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Could not load element: " + fxmlPath);
            }
        }
    }

    private void refreshCurrentView() {
        // preserve current search text and filter during auto-refresh
        applySearchAndFilter(searchField != null ? searchField.getText() : "");
    }
}
