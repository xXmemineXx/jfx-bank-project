package com.bank.dao;

//client DAO(data structure object)
//all sql related methodes should be in here for the client model

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.bank.database.DbConnection;
import com.bank.models.Client;

public class ClientDAO
{
    // CREATE
    public boolean ajouter(Client c) {
        String sql = "INSERT INTO clients (account_id, first_name, last_name, phone, email, balance) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection(); //ferme automatiquement les codes à la fin du bloc, même en cas d'erreur (try-with-resources)
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.get_id());
            ps.setString(2, c.get_first_name());
            ps.setString(3, c.get_last_name());
            ps.setString(4, c.get_phone());
            ps.setString(5, c.get_mail());
            ps.setInt(6, c.get_balance());

            return ps.executeUpdate() > 0; //exécute la requête et retourne le nombre de lignes affectées

        } catch (SQLException e) {
            System.out.println("Erreur ajout client : " + e.getMessage());
            return false;
        }
    }

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

    // UPDATE
    public boolean modifier(Client c) {
        String sql = "UPDATE clients SET first_name = ?, last_name = ?, phone = ?, email = ?, balance = ? WHERE account_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.get_first_name());
            ps.setString(2, c.get_last_name());
            ps.setString(3, c.get_phone());
            ps.setString(4, c.get_mail());
            ps.setInt(5, c.get_balance());
            ps.setString(6, c.get_id());


            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Erreur modification client : " + e.getMessage());
            return false;
        }
    }

    // DELETE
    public boolean supprimer(String account_id) {
        String sql = "DELETE FROM clients WHERE account_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, account_id);
            boolean deleted = ps.executeUpdate() > 0;
            
            if (deleted) {
                System.out.println("Client supprimé avec succès.");
            }
            return deleted;

        } catch (SQLException e) {
            System.out.println("Erreur suppression client : " + e.getMessage());
            return false;
        }
    }

    // READ - recherche par numéro de compte ou nom (LIKE)
    public List<Client> rechercher(String motCle) {
        List<Client> liste = new ArrayList<>();
        String sql = "SELECT * FROM clients WHERE account_id LIKE ? OR first_name LIKE ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String recherche = "%" + motCle + "%";
            ps.setString(1, recherche);
            ps.setString(2, recherche);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Client c = new Client(
                    rs.getString("account_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getInt("balance")
                );
                liste.add(c);
            }

        } catch (SQLException e) {
            System.out.println("Erreur recherche client : " + e.getMessage());
        }
        return liste;
    }
}