package com.bank.dao;

//client DAO(data structure object)
//all sql related methodes should be in here for the client model

import com.bank.database.DbConnection;
import com.bank.models.Client;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO
{
	//get all users
    public List<Client> getAllClients() 
    {
        List<Client> clientList = new ArrayList<>();
        String sql = "SELECT * FROM clients";

        // Open connection and execute query
        try (
        	Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

        // insert client object to the returned list
            while (rs.next()) {
                Client client = new Client(
                    rs.getString("account_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getInt("balance")
                );
                clientList.add(client);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clientList;
    }
}