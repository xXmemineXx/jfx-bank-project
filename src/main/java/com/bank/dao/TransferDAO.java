package com.bank.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.bank.database.DbConnection;
import com.bank.models.Transfer;

public class TransferDAO
{
    // CREATE
    public boolean ajouter(Transfer t) {
        String sql = "INSERT INTO transfer (transfer_id, sender_id, receiver_id, amount, transfer_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection(); //ferme automatiquement les codes à la fin du bloc, même en cas d'erreur (try-with-resources)
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, t.get_id());
            ps.setString(2, t.get_sender());
            ps.setString(3, t.get_receiver());
            ps.setInt(4, t.get_amount());
            ps.setTimestamp(5, Timestamp.valueOf(t.get_date()));

            return ps.executeUpdate() > 0; //exécute la requête et retourne le nombre de lignes affectées

        } catch (SQLException e) {
            System.out.println("Erreur ajout transfer : " + e.getMessage());
            return false;
        }
    }

	//get all returns
    public List<Transfer> getAllTransfers() 
    {
        List<Transfer> transfersList = new ArrayList<>();
        String sql = "SELECT * FROM transfer";

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
                    rs.getString("receiver_id"),
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
    public boolean supprimer(String transfer_id) {
        String sql = "DELETE FROM transfer WHERE transfer_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, transfer_id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Erreur suppression transfer : " + e.getMessage());
            return false;
        }
    }
}