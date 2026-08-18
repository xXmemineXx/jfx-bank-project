package com.bank.controller;


import com.bank.dao.HistoryDAO;
import com.bank.models.History;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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

    // Instantiate your DAO
    private final HistoryDAO historyDAO = new HistoryDAO();

    @FXML
    public void initialize() {
        // 1. Link your columns to the model properties (must match getter name suffixes)
        objectColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("operation"));
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        targetColumn.setCellValueFactory(new PropertyValueFactory<>("target"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        // 2. Load data from the database using your method
        List<History> databaseRecords = historyDAO.gethistory();

        // 3. Wrap the results and assign them directly to the visual elements
        ObservableList<History> tableItems = FXCollections.observableArrayList(databaseRecords);
        historyTable.setItems(tableItems);
    }
}
