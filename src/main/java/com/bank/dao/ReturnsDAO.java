package com.bank.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.bank.database.DbConnection;
import com.bank.models.Returns;

public class ReturnsDAO
{
    // CREATE
    public boolean ajouter(Returns r) {
        String sql = "INSERT INTO returned (return_id, loan_id, fully_returned, unpayed, returned_amount, return_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection(); //ferme automatiquement les codes à la fin du bloc, même en cas d'erreur (try-with-resources)
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, r.get_id());
            ps.setString(2, r.get_loan());
            ps.setBoolean(3, r.is_repayed());
            ps.setInt(4, r.get_unpayed());
            ps.setInt(5, r.get_amount());
            ps.setTimestamp(6, Timestamp.valueOf(r.get_date()));

            return ps.executeUpdate() > 0; //exécute la requête et retourne le nombre de lignes affectées

        } catch (SQLException e) {
            System.out.println("Erreur ajout return : " + e.getMessage());
            return false;
        }
    }

	//get all returns
    public List<Returns> getAllReturns() 
    {
        List<Returns> returnsList = new ArrayList<>();
        String sql = "SELECT * FROM returned";

        // Open connection and execute query
        try (
        	Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

        // insert Returns object to the returned list
            while (rs.next()) {
                Returns returned = new Returns(
                    rs.getBoolean("fully_returned"),
                    rs.getInt("returned_amount"),
                    rs.getInt("unpayed"),
                    rs.getString("return_id"),
                    rs.getString("loan_id"),
                    rs.getTimestamp("return_date").toLocalDateTime()
                );
                returnsList.add(returned);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnsList;
    }

    // UPDATE
    public boolean modifier(Returns r) {
        String sql = "UPDATE returned SET loan_id = ?, fully_returned = ?, unpayed = ?, returned_amount = ?, return_date = ? WHERE return_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, r.get_loan());
            ps.setBoolean(2, r.is_repayed());
            ps.setInt(3, r.get_unpayed());
            ps.setInt(4, r.get_amount());
            ps.setTimestamp(5, Timestamp.valueOf(r.get_date()));
            ps.setString(6, r.get_id());


            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Erreur modification return : " + e.getMessage());
            return false;
        }
    }

    // DELETE
    public boolean supprimer(String return_id) {
        String sql = "DELETE FROM returned WHERE return_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, return_id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Erreur suppression return : " + e.getMessage());
            return false;
        }
    }

    //List des PRETE pour chaque SITUATION
    public List<Returns> listerParSituation(boolean fully_returned) {
        List<Returns> liste = new ArrayList<>();
        String sql = "SELECT * FROM returned WHERE fully_returned = ?";

        try (Connection conn = DbConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, fully_returned);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Returns r = new Returns(
                    rs.getBoolean("fully_returned"),
                    rs.getInt("returned_amount"),
                    rs.getInt("unpayed"),
                    rs.getString("return_id"),
                    rs.getString("loan_id"),
                    rs.getTimestamp("return_date").toLocalDateTime()
                );
                liste.add(r);
            }

        } catch (SQLException e) {
            System.out.println("Erreur listage par situation : " + e.getMessage());
        }
        return liste;
    }
}