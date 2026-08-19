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

    // UPDATE
    public boolean modifier(Transfer t) {
        String sql = "UPDATE transfer SET sender_id = ?, receiver_id = ?, amount = ?, transfer_date = ? WHERE transfer_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.get_sender());
            ps.setString(2, t.get_receiver());
            ps.setInt(3, t.get_amount());
            ps.setTimestamp(4, Timestamp.valueOf(t.get_date()));
            ps.setInt(5, t.get_id());

            
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Erreur modification transfer : " + e.getMessage());
            return false;
        }
    }

    // DELETE
    public boolean supprimer(int transfer_id) {
        String sql = "DELETE FROM transfer WHERE transfer_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, transfer_id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Erreur suppression transfer : " + e.getMessage());
            return false;
        }
    }

    // CREATE
    public boolean ajouter(Transfer t) {
        String sql = "INSERT INTO transfer (sender_id, receiver_id, amount) VALUES (?, ?, ?)";
        try (Connection conn = DbConnection.getConnection(); //ferme automatiquement les codes à la fin du bloc, même en cas d'erreur (try-with-resources)
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.get_sender());
            ps.setString(2, t.get_receiver());
            ps.setInt(3, t.get_amount());

            return ps.executeUpdate() > 0; //exécute la requête et retourne le nombre de lignes affectées

        } catch (SQLException e) {
            System.out.println("Erreur ajout transfer : " + e.getMessage());
            return false;
        }
    }

    /** Case-insensitive search across transfer id, sender/receiver ids and names. */
    public List<Transfer> searchTransfers(String query) {
        List<Transfer> results = new ArrayList<>();
        String sql = "SELECT "
                   + "t.transfer_id, t.amount, t.transfer_date, "
                   + "t.sender_id, c_sender.first_name AS sender_first_name, "
                   + "c_sender.last_name AS sender_last_name, "
                   + "t.receiver_id, c_receiver.first_name AS receiver_first_name, "
                   + "c_receiver.last_name AS receiver_last_name "
                   + "FROM transfer t "
                   + "JOIN clients c_sender ON t.sender_id = c_sender.account_id "
                   + "JOIN clients c_receiver ON t.receiver_id = c_receiver.account_id "
                   + "WHERE CAST(t.transfer_id AS TEXT) LIKE ? "
                   + "OR LOWER(t.sender_id) LIKE ? "
                   + "OR LOWER(t.receiver_id) LIKE ? "
                   + "OR LOWER(c_sender.first_name) LIKE ? "
                   + "OR LOWER(c_sender.last_name) LIKE ? "
                   + "OR LOWER(c_receiver.first_name) LIKE ? "
                   + "OR LOWER(c_receiver.last_name) LIKE ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String formatted = "%" + query.toLowerCase().trim() + "%";
            for (int i = 1; i <= 7; i++) {
                stmt.setString(i, formatted);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new Transfer(
                        rs.getInt("transfer_id"),
                        rs.getInt("amount"),
                        rs.getString("sender_id"),
                        rs.getString("sender_first_name"),
                        rs.getString("receiver_id"),
                        rs.getString("receiver_first_name"),
                        rs.getTimestamp("transfer_date").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
}