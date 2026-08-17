package com.bank.dao;

import com.bank.database.DbConnection;
import com.bank.models.History;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryDAO
{
	//get history
    public List<History> gethistory() 
    {
        List<History> transfersList = new ArrayList<>();
        String sql = "SELECT * FROM history";

        // Open connection and execute query
        try (
        	Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

        // insert transfer object to the returned list
            while (rs.next()) {
                History history = new History(
                    rs.getString("from_"),
                    rs.getString("operation_"),
                    rs.getString("subject_"),
                    rs.getString("target_"),
                    rs.getTimestamp("date_").toLocalDateTime()
                );
                transfersList.add(history);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transfersList;
    }
}