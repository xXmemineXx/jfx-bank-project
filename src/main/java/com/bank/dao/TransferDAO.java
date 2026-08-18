package com.bank.dao;

import com.bank.database.DbConnection;
import com.bank.models.Transfer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransferDAO
{
	//get all returns
    public List<Transfer> getAllTransfers() 
    {
        List<Transfer> transfersList = new ArrayList<>();
        String sql = "SELECT " +
                        "t.transfer_id, " +
                        "t.amount, " +
                        "t.transfer_date, " +
                        "t.sender_id, " +
                        "c_sender.first_name AS sender_first_name, " +
                        "c_sender.last_name AS sender_last_name,  " +
                        "t.receiver_id, " +
                        "c_receiver.first_name AS receiver_first_name, "+
                        "c_receiver.last_name AS receiver_last_name "+
                    "FROM transfer t "+
                    "JOIN clients c_sender ON t.sender_id = c_sender.account_id "+
                    "JOIN clients c_receiver ON t.receiver_id = c_receiver.account_id";

        // Open connection and execute query
        try (
        	Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

        // insert transfer object to the returned list
            while (rs.next()) {
                Transfer transfer = new Transfer(
                    rs.getInt("transfer_id"),
                    rs.getInt("amount"),
                    rs.getString("sender_id"),
                    rs.getString("sender_first_name"),
                    rs.getString("receiver_id"),
                    rs.getString("receiver_first_name"),
                    rs.getTimestamp("transfer_date").toLocalDateTime()
                );
                transfersList.add(transfer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transfersList;
    }
}