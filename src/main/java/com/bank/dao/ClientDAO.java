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

    //get a user by his ID
    public Client getClient(String id)
    {
        String sql = "SELECT * FROM clients WHERE account_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                // If a record exists, map the columns to the Model constructor
                if (rs.next()) {
                    return new Client(
                        rs.getString("account_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getInt("balance")
                    );
                }
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * INSERT a completely new client into the database.
     */
    public boolean insertClient(Client client) {
        String sql = "INSERT INTO clients (account_id, last_name, first_name, email, phone, balance) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, client.get_id());
            stmt.setString(2, client.get_last_name());
            stmt.setString(3, client.get_first_name());
            stmt.setString(4, client.get_mail());
            stmt.setString(5, client.get_phone());
            stmt.setInt(6, client.get_balance());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Returns true if insertion succeeded
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * UPDATE an existing client's details in the database.
     */
    public boolean updateClient(Client client) {
        String sql = "UPDATE clients SET last_name = ?, first_name = ?, email = ?, phone = ? WHERE account_id = ?";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, client.get_last_name());
            stmt.setString(2, client.get_first_name());
            stmt.setString(3, client.get_mail());
            stmt.setString(4, client.get_phone());
            stmt.setString(5, client.get_id()); // Where clause constraints parameter
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Returns true if update succeeded
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Client> searchClients(String query) {
        List<Client> resultsList = new ArrayList<>();
        // Using LOWER() ensures the search remains completely case-insensitive
        String sql = "SELECT * FROM clients WHERE LOWER(account_id) LIKE ? " +
                     "OR LOWER(first_name) LIKE ? " +
                     "OR LOWER(last_name) LIKE ? " +
                     "ORDER BY account_id ASC";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Wrap the query search parameter with percentage wildcard tags
            String formattedQuery = "%" + query.toLowerCase().trim() + "%";
            
            stmt.setString(1, formattedQuery);
            stmt.setString(2, formattedQuery);
            stmt.setString(3, formattedQuery);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Client client = new Client(
                        rs.getString("account_id"),
                        rs.getString("last_name"),
                        rs.getString("first_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getInt("balance")
                    );
                    resultsList.add(client);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultsList;
    }
}