package com.bank.controller;

import com.bank.models.Admins;
import com.bank.App;
import com.bank.dao.HistoryDAO;
import com.bank.models.History;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDateTime;
import java.util.List;

public class HomeController {

    @FXML
    private TableView<History> historyTable;
    @FXML
    private TableColumn<History, String> objectColumn;
    @FXML
    private TableColumn<History, String> actionColumn;
    @FXML
    private TableColumn<History, String> subjectColumn;
    @FXML
    private TableColumn<History, String> targetColumn;
    @FXML
    private TableColumn<History, LocalDateTime> dateColumn;
    @FXML
    private Label adminLabel;

    // Instantiate your DAO
    private final HistoryDAO historyDAO = new HistoryDAO();

    @FXML
    public void initialize() {
        // Link your columns to the model properties
        objectColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("operation"));
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        targetColumn.setCellValueFactory(new PropertyValueFactory<>("target"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Load data from database
        List<History> databaseRecords = historyDAO.gethistory();

        ObservableList<History> tableItems = FXCollections.observableArrayList(databaseRecords);
        historyTable.setItems(tableItems);

        Admins user = App.getCurrentLoggedInUser();

        if (user != null)
        {
            adminLabel.setText(user.get_first_name() + " " + user.get_last_name());
        }
    }
}
